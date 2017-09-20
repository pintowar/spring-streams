package com.poc.demo;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface MultiInputSink {
    String INPUT1 = "input1";

    String INPUT2 = "input2";

    @Input(INPUT1)
    SubscribableChannel input1();

    @Input(INPUT2)
    SubscribableChannel input2();
}
