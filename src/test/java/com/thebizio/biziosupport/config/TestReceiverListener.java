package com.thebizio.biziosupport.config;

import com.thebizio.biziosupport.util.mq.Listener;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RabbitListenerTest
public class TestReceiverListener {

    @Bean
    Listener listener() {
        return new Listener();
    }
}
