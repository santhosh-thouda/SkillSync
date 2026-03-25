package com.capgemini.session.service;

import com.capgemini.session.event.SessionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionMessagePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private SessionMessagePublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SessionMessagePublisher(rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "exchange", "skill-sync-exchange");
        ReflectionTestUtils.setField(publisher, "routingKey", "session.created");
    }

    @Test
    void publishSessionEventShouldSendMessageToRabbitMq() {
        SessionEvent event = SessionEvent.builder()
                .sessionId(1L)
                .mentorId(5L)
                .learnerId(9L)
                .status("REQUESTED")
                .build();

        publisher.publishSessionEvent(event);

        verify(rabbitTemplate).convertAndSend("skill-sync-exchange", "session.created", event);
    }
}
