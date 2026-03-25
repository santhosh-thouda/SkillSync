package com.capgemini.notification.service;

import com.capgemini.notification.event.SessionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationListener {

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleSessionEvent(SessionEvent event) {
        log.info("Received session event via RabbitMQ: {}", event);

        // In a real application, here we would send an email or push notification.
        switch (event.getStatus()) {
            case "REQUESTED":
                log.info("Sending email to Mentor {} about new session request from Learner {}", event.getMentorId(), event.getLearnerId());
                break;
            case "ACCEPTED":
                log.info("Sending email to Learner {} that their session with Mentor {} was accepted", event.getLearnerId(), event.getMentorId());
                break;
            case "REJECTED":
                log.info("Sending email to Learner {} that their session with Mentor {} was rejected", event.getLearnerId(), event.getMentorId());
                break;
            case "CANCELLED":
                log.info("Sending cancellation emails to Mentor {} and Learner {}", event.getMentorId(), event.getLearnerId());
                break;
            default:
                log.info("Processing generic session event: {}", event.getStatus());
                break;
        }
    }
}
