package com.poc.demo

import groovy.util.logging.Slf4j
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message

@EnableBinding(MultiInputSink.class)
@Slf4j
class SampleSink {

    @StreamListener(MultiInputSink.INPUT2)
    synchronized void receive1(Message<String> message) {
        def key = new String(message.headers[KafkaHeaders.RECEIVED_MESSAGE_KEY] as byte[], 'UTF-8')
        log.info("$key | ${message.payload}")
    }

}
