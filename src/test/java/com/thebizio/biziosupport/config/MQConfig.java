package com.thebizio.biziosupport.config;

import com.thebizio.biziosupport.util.mq.Listener;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

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
}
