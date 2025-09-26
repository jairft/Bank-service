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
    
    private void processUserEvent(UserCreatedEvent event) {
        if (event.getUserId() == null) {
            log.warn("Event received with null userId");
            return;
        }
        
        if (event.getEmail() == null || event.getEmail().isEmpty()) {
            log.warn("Event received with empty email for userId: {}", event.getUserId());
            return;
        }
        
        if (event.getCpf() == null || event.getCpf().length() < 6) {
            log.warn("Event received with invalid CPF for userId: {}", event.getUserId());
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
        
        // GERA SENHA COM OS PRIMEIROS 6 DÍGITOS DO CPF
        String initialPassword = generateInitialPassword(event.getCpf());
        String encodedPassword = passwordEncoder.encode(initialPassword);
        
        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(event.getUserId());
        credentials.setEmail(event.getEmail());
        credentials.setPassword(encodedPassword);
        credentials.setStatus(UserStatus.PENDING_ACTIVATION); // Precisa ativar
        credentials.setActivationToken(generateActivationToken());
        credentials.setActivationExpires(LocalDateTime.now().plusHours(24));
        
        credentialsRepository.save(credentials);
        
        log.info("✅ USUÁRIO CRIADO - AGUARDANDO ATIVAÇÃO");
        log.info("📧 Email: {}", event.getEmail());
        log.info("🔢 CPF: {}", event.getCpf());
        log.info("🔑 Senha Temporária: {}", initialPassword);
        log.info("🔐 Token de Ativação: {}", credentials.getActivationToken());
        log.info("💡 Ative a conta e depois faça login com os 6 dígitos do CPF!");
            log.info("=========================================");
    }

    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }
    
    private String generateInitialPassword(String cpf) {
        // Remove caracteres não numéricos (caso o CPF tenha formatação)
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        
        // Pega os primeiros 6 dígitos do CPF
        if (cleanCpf.length() >= 6) {
            return cleanCpf.substring(0, 6);
        } else {
            // Fallback se o CPF for muito curto
            log.warn("CPF muito curto: {}. Usando fallback.", cpf);
            return "123456"; // Senha padrão de fallback
        }
    }
}