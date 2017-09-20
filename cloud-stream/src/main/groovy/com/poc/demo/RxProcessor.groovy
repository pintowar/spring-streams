package com.poc.demo

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.integration.support.MessageBuilder
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import rx.Observable as Obs

import java.util.concurrent.TimeUnit

@EnableBinding([MultiOutputSource.class, MultiInputSink.class])
@EnableAutoConfiguration
class RxProcessor {

    @StreamListener
    @Output(MultiOutputSource.OUTPUT2)
    Obs<Message<String>> receive(@Input(MultiInputSink.INPUT1) Obs<Message<String>> input) {
        input.buffer(2, TimeUnit.SECONDS).flatMap { el ->
            Obs.from(el).groupBy { it.headers[KafkaHeaders.RECEIVED_MESSAGE_KEY] as List<Byte> }.flatMap { kv ->
                kv.map { it.payload }.reduce { a, b -> "$a *** $b" }.map {
                    MessageBuilder.withPayload(it)
                            .setHeader(KafkaHeaders.MESSAGE_KEY, kv.key as byte[]).build()
                }
            }
        }
    }
}
