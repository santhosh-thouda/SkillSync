package com.capgemini.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routingkey}")
    private String routingKey;

    /**
     * Declare the queue as durable so it survives RabbitMQ restarts.
     * Using durable=true, exclusive=false, autoDelete=false matches
     * the declaration in session-service — both services agree on the
     * same queue properties, so whichever starts first creates it.
     */
    @Bean
    public Queue sessionQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange sessionExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding sessionBinding(Queue sessionQueue, TopicExchange sessionExchange) {
        return BindingBuilder
                .bind(sessionQueue)
                .to(sessionExchange)
                .with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
