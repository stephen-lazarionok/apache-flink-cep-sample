package com.slaz.flinksample.app;

import com.slaz.flinksample.PhoneCallEvent;
import com.slaz.flinksample.SamplePhoneCallEventsGenerator;
import org.apache.commons.net.nntp.Threadable;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import com.slaz.flinksample.domain.package$;

public class PhoneCallsStream extends RichParallelSourceFunction<PhoneCallEvent> {

    private volatile boolean running = true;
    private long pauseMs = 500L;

    @Override
    public void run(SourceContext<PhoneCallEvent> sourceContext) throws Exception {
        while (this.running) {

            sourceContext.collect( SamplePhoneCallEventsGenerator.genNext() );

            Thread.sleep(pauseMs);
        }
    }

    @Override
    public void cancel() {
        this.running = false;
    }
}
