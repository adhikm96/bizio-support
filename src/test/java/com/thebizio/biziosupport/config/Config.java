package com.thebizio.biziosupport.config;

import com.thebizio.biziosupport.util.mq.Listener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RabbitListenerTest
public class Config {

    @Bean
    DirectExchange emailExchange() {
        return new DirectExchange("email-exchange");
    }

    @Bean
    Queue emailQueue() {
        return new Queue("email-queue");
    }

    @Bean
    Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with("bizio.email");
    }


    @Bean
    DirectExchange bzEventExchange() {
        return new DirectExchange("bz.events");
    }

    @Bean
    Queue bzEventQueue() {
        return new Queue("bz-queue");
    }

    @Bean
    Binding bzEventBinding(Queue bzEventQueue, DirectExchange bzEventExchange) {
        return BindingBuilder.bind(bzEventQueue).to(bzEventExchange).with("bz.support");
    }

    @Bean
    public Listener listener() {
        return new Listener();
    }
}
