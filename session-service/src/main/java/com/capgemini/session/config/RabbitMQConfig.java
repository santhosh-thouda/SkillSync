package com.capgemini.session.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Value("${rabbitmq.queue:session.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange:session.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routingkey:session.routingkey}")
    private String routingKey;

    // ── Queue / Exchange / Binding ────────────────────────────
    // Declaring these here ensures the queue exists as soon as
    // session-service connects, regardless of startup order.

    @Bean
    public Queue sessionQueue() {
        return new Queue(queueName, true); // durable=true
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

    // ── Connection / Template ─────────────────────────────────

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.getRabbitConnectionFactory().setConnectionTimeout(3000);
        factory.getRabbitConnectionFactory().setRequestedHeartbeat(10);
        factory.setPublisherReturns(false);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(false);
        return template;
    }
}
