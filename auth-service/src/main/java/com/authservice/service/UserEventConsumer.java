package com.authservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.authservice.event.UserCreatedEvent;
import com.authservice.event.UserUpdatedEvent;
import com.authservice.model.UserCredentials;
import com.authservice.model.UserStatus;
import com.authservice.repository.UserCredentialsRepository;

@Service
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);
    private final UserCredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEventConsumer(UserCredentialsRepository credentialsRepository,
                             PasswordEncoder passwordEncoder) {
        this.credentialsRepository = credentialsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @KafkaListener(
        topics = "user-events",
        groupId = "auth-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeUserCreatedEvent(
            @Payload(required = false) UserCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        if (event == null) {
            log.warn("Received null or invalid event from partition {} offset {}", partition, offset);
            return;
        }

        try {
            log.info("Processing user event: {} (Key: {}) from partition {} offset {}",
                    event.getEmail(), key, partition, offset);

            processUserEvent(event);

        } catch (Exception e) {
            log.error("Error processing event for user {}: {}",
                     event != null ? event.getEmail() : "unknown", e.getMessage());
        }
    }

    @KafkaListener(
            topics = "user-updated-events",
            groupId = "auth-service-group",
            containerFactory = "kafkaListenerContainerFactoryUserUpdated"
    )
    @Transactional
    public void consumeUserUpdatedEvent(
            @Payload(required = false) UserUpdatedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        if (event == null) {
            log.warn("Received null UserUpdatedEvent");
            return;
        }

        log.info("📨 Mensagem USER_UPDATED recebida: {}", event.getEmail());

        credentialsRepository.findByUserId(event.getUserId()).ifPresent(credentials -> {
            if (event.getEmail() != null && !event.getEmail().equals(credentials.getEmail())) {
                credentials.setEmail(event.getEmail());
                credentialsRepository.save(credentials);
                log.info("✏️ Email atualizado no auth-service: {}", event.getEmail());
            } else {
                log.info("Nenhuma alteração de email detectada para usuário {}", event.getUserId());
            }
        });
    }



    private void processUserEvent(UserCreatedEvent event) {
        if (event.getUserId() == null || event.getEmail() == null || event.getEmail().isEmpty()) {
            log.warn("Invalid event data for userId: {}", event.getUserId());
            return;
        }

        if (credentialsRepository.existsByUserId(event.getUserId())) {
            log.info("Credentials already exist for user ID: {}", event.getUserId());
            return;
        }

        if (credentialsRepository.existsByEmail(event.getEmail())) {
            log.info("Credentials already exist for email: {}", event.getEmail());
            return;
        }

        if (event.getPassword() == null || event.getPassword().isEmpty()) {
            throw new RuntimeException("Senha não fornecida pelo serviço de usuários para o email: " + event.getEmail());
        }

        // Usa a senha recebida do outro serviço

        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(event.getUserId());
        credentials.setEmail(event.getEmail());
        credentials.setPassword(passwordEncoder.encode(event.getPassword())); // hash
        credentials.setStatus(UserStatus.PENDING_ACTIVATION);
        credentials.setActivationToken(generateActivationToken());
        credentials.setActivationExpires(LocalDateTime.now().plusHours(24));

        credentialsRepository.save(credentials);

        log.info("✅ USUÁRIO CRIADO - AGUARDANDO ATIVAÇÃO");
        log.info("📧 Email: {}", event.getEmail());
        log.info("🔐 Token de Ativação: {}", credentials.getActivationToken());
        log.info("💡 Ative a conta e depois faça login com a senha");
        log.info("=========================================");
    }

    private String generateActivationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
