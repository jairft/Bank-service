package com.accountservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.event.UserCreatedEvent;
import com.accountservice.event.UserUpdatedEvent;
import com.accountservice.model.Account;

@Service
public class UserEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);
    private final AccountService accountService;
    
    public UserEventConsumer(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @KafkaListener(
        topics = "user-events",
        groupId = "account-service-group",
        containerFactory = "userCreatedKafkaListenerFactory"
    )
    @Transactional
    public void consumeUserCreatedEvent(UserCreatedEvent event) {
        log.info("📨 Novo usuário recebido: {}", event.getEmail());

        Account account = accountService.createAccountForUser(
            event.getUserId(),
            event.getNome(),
            event.getCpf(),
            event.getEmail(),
            event.getTelefone(),
            Account.AccountType.CORRENTE
        );

        log.info("✅ Conta criada - Agência: {}, Conta: {}, Titular: {}, Email: {}",
                account.getAgencyNumber(),
                account.getAccountNumber(),
                account.getUserName(),
                account.getUserEmail());
    }

    @KafkaListener(
        topics = "user-updated-events",
        groupId = "account-service-group",
        containerFactory = "userUpdatedKafkaListenerFactory"
    )
    @Transactional
    public void consumeUserUpdatedEvent(UserUpdatedEvent event) {
        log.info("📨 Atualização de usuário recebida: {}", event.getUserId());

        Account account = accountService.updateAccountUser(
            event.getUserId(),
            event.getEmail(),
            event.getTelefone()
        );

        log.info("✅ Usuário atualizado - userId: {}, Email: {}, Telefone: {}",
                account.getUserId(),
                account.getUserEmail(),
                account.getUserPhone());
    }
}
