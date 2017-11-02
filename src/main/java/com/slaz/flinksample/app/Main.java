package com.slaz.flinksample.app;


import com.slaz.flinksample.PhoneCallEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.IngestionTimeExtractor;

import java.util.Map;

public class Main {

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStream<PhoneCallEvent> phoneCalls = env
                .addSource(new PhoneCallsStream())
                .assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());

        /***************************
         * [START]: Identify 3 failed phone calls in a row from the same user
         ***************************/
        DataStream<PhoneCallEvent> phoneCallsPartionedByCaller = phoneCalls.keyBy(e -> e.from());

        final FilterFunction<PhoneCallEvent> failedCall = e -> e.isFailed();

        Pattern<PhoneCallEvent, ?> threeFailedCallsInARow =
                Pattern.<PhoneCallEvent>begin("first").where(failedCall)
                .next("second").where(failedCall)
                .next("third").where(failedCall);

        CEP.pattern(phoneCallsPartionedByCaller, threeFailedCallsInARow).select(pattern -> {
            printThreeFailedCallsPattern(pattern);
            return pattern;
        });

        /***************************
         * [END]: Identify 3 failed phone calls in a row from the same user
         ***************************/


        // Print all the events
        phoneCalls.addSink(phoneCallEvent -> System.out.println(phoneCallEvent));


        env.execute("Monitor calls");
    }

    private static void printThreeFailedCallsPattern(final Map<String, PhoneCallEvent> patternMap) {
        final StringBuilder sb = new StringBuilder("***********************\n");
        sb.append("* Identified 3 failed phone calls in a row: \n");
        sb.append("* 1. " + patternMap.get("first") + "\n");
        sb.append("* 2. " + patternMap.get("second")+ "\n");
        sb.append("* 3. " + patternMap.get("third") + "\n");
        sb.append("***********************\n");

        System.out.println(sb.toString());
    }
}
