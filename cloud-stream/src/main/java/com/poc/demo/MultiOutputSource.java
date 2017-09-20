package com.poc.demo;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface    MultiOutputSource {
    String OUTPUT1 = "output1";

    String OUTPUT2 = "output2";

    @Output(OUTPUT1)
    MessageChannel output1();

    @Output(OUTPUT2)
    MessageChannel output2();
}
