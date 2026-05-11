package com.capgemini.session.service;

import com.capgemini.session.event.SessionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routingkey}")
    private String routingKey;

    public void publishSessionEvent(SessionEvent event) {
        try {
            log.info("Publishing session event: {}", event);
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            // RabbitMQ is optional — log and continue so session operations are not blocked
            log.warn("Failed to publish session event (RabbitMQ may be unavailable): {}", e.getMessage());
        }
    }
}
