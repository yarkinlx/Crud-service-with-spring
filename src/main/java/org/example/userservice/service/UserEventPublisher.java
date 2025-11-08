package org.example.userservice.service;

import org.example.userservice.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(UserEventPublisher.class);

    @Value("${app.kafka.topic.user-events}")
    private String userEventsTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserEvent(UserEvent userEvent) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(userEventsTopic, userEvent.getEmail(), userEvent);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent user event: {} with offset: {}",
                            userEvent, result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send user event: {} due to: {}",
                            userEvent, ex.getMessage());
                }
            });
        } catch (Exception ex) {
            logger.error("Error publishing user event: {}", ex.getMessage());
        }
    }
}