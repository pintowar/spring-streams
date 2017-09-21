package com.poc.demo

import com.github.javafaker.Faker
import groovy.util.logging.Slf4j
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Bean
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.integration.annotation.Poller
import org.springframework.integration.core.MessageSource
import org.springframework.integration.support.MessageBuilder
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message

@EnableBinding(MultiOutputSource.class)
@Slf4j
class SampleSource {

    @Bean
    @InboundChannelAdapter(value = MultiOutputSource.OUTPUT1, poller = @Poller(fixedDelay = "500", maxMessagesPerPoll = "1"))
    synchronized MessageSource<String> messageSource() {
        Faker fkr = new Faker()
        new MessageSource<String>() {
            Message<String> receive() {
                def data = Math.random() <= 0.5 ? ['GoT', fkr.gameOfThrones().quote()] : ['Chuck', fkr.chuckNorris().fact()]
                MessageBuilder.withPayload(data.last())
                        .setHeader(KafkaHeaders.MESSAGE_KEY, data.first().bytes).build()
            }
        }
    }
}
