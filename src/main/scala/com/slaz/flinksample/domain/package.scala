package com.slaz.flinksample

import com.slaz.flinksample.domain.Phone
import org.scalacheck.Gen

/**
  * Created by Stephen Lazarionok.
  */
package object domain {

    type Phone = String

}

case class PhoneCallEvent(from: Phone, to: Phone, result: PhoneCallResult) {
    override def toString: String = {
        val basis = s"Phone call from '$from' to '$to'"
        result match {
            case SuccessPhoneCall(d) => s"$basis for $d seconds."
            case FailPhoneCall => s"$basis failed."
        }
    }

    def isFailed = result.getClass == FailPhoneCall.getClass
}

sealed trait PhoneCallResult

case class SuccessPhoneCall(durationSeconds: Long) extends PhoneCallResult

case object FailPhoneCall extends PhoneCallResult { def instance = this }


object SamplePhoneCallEventsGenerator {

    val prefixGen = Gen.oneOf("+1914", "+1440", "+1201")
    val suffixGen = Gen.oneOf("111", "222", "333", "444", "555")
    val endingGen = Gen.oneOf("666", "777", "888", "999", "000")

    val phoneGen = for {
        p <- prefixGen
        s <- suffixGen
        e <- endingGen
    } yield s"$p.$s.$e"

    val successPhoneGen = for {
        d <- Gen.choose(10, 60)
    } yield SuccessPhoneCall(d)

    val resultGen = Gen.frequency(
        (2, successPhoneGen),
        (1, FailPhoneCall)
    )

    val generator = for {
        f <- phoneGen
        t <- phoneGen
        if f != t
        r <- resultGen
    } yield PhoneCallEvent(f, t, r)

    def genNext():PhoneCallEvent = {
        generator.sample match {
            case Some(x) => x
            case None => genNext()
        }
    }
}
