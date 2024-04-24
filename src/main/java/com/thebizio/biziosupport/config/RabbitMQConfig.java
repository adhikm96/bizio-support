package com.thebizio.biziosupport.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${hostname}")
    private String hostname;

    @Bean
    DirectExchange gossipExchange() { return new DirectExchange("bz.gossip"); }

    @Bean
    Queue gossipQueue() { return new Queue(hostname, false, false, true); }

    @Bean
    Binding gossipBinding1(Queue gossipQueue, DirectExchange gossipExchange) {
        return BindingBuilder.bind(gossipQueue).to(gossipExchange).with("BZ-ADMIN|CTR|TICKET");
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}