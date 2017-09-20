package com.poc;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KafkaTreatment {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTreatment.class);

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 500)
    public void sendMessage() {
        Faker fkr = new Faker();
        String topic = "streamingTopic1";
        if (Math.random() <= 0.5) kafkaTemplate.send(topic, "GoT", fkr.gameOfThrones().quote());
        else kafkaTemplate.send(topic, "Chuck", fkr.chuckNorris().fact());
//        LOGGER.info("Message sent");
    }

    @KafkaListener(id = "consumer", topics = "streamingTopic2")
    public void consume(String msg, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        LOGGER.info(key + " | " + msg);
    }
}
