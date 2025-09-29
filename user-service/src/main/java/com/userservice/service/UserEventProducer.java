package com.userservice.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.userservice.event.UserCreatedEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserEventProducer {

    private static final String USER_EVENTS_TOPIC = "user-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(USER_EVENTS_TOPIC, event.getUserId().toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Evento USER_CREATED enviado com sucesso para o usuário: {}", event.getUserId());
                    log.debug("Offset: {}, Partition: {}",
                            result.getRecordMetadata().offset(),
                            result.getRecordMetadata().partition());
                } else {
                    log.error("Falha ao enviar evento USER_CREATED para o usuário: {}", event.getUserId(), ex);
                }
            });

        } catch (Exception ex) {
            log.error("Erro ao tentar enviar evento USER_CREATED: {}", ex.getMessage(), ex);
        }
    }
}

