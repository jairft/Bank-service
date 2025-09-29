package com.accountservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.model.Account;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);
    private final AccountService accountService;
    private final ObjectMapper objectMapper;
    
    public UserEventConsumer(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }
    
    @KafkaListener(
        topics = "user-events",
        groupId = "account-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeUserCreatedEvent(String message) {
        log.info("📨 Mensagem JSON recebida do Kafka");
        log.debug("Conteúdo: {}", message);
        
        try {
            processMessage(message);
        } catch (Exception e) {
            log.error("❌ Erro ao processar mensagem: {}", e.getMessage());
            log.debug("Stack trace completo:", e);
        }
    }
    
    private void processMessage(String jsonMessage) {
        try {
            // Extrai campos do JSON
            String userIdStr = extractJsonField(jsonMessage, "userId");
            String nome = extractJsonField(jsonMessage, "nome");
            String cpf = extractJsonField(jsonMessage, "cpf");
            String email = extractJsonField(jsonMessage, "email");
            String telefone = extractJsonField(jsonMessage, "telefone");
            
            if (userIdStr != null && nome != null && cpf != null && email != null) {
                Long userId = Long.parseLong(userIdStr);
                
                System.out.println("👤 Processando usuário: " + nome + " (" + email + ")");
                
                // ✅ CRIA CONTA COM AGÊNCIA PADRÃO
                Account account = accountService.createAccountForUser(
                    userId, nome, cpf, email, telefone, Account.AccountType.CORRENTE
                );
                
                System.out.println("✅ CONTA BANCÁRIA CRIADA");
                System.out.println("   🏦 Agência: " + account.getAgencyNumber());
                System.out.println("   📊 Conta: " + account.getAccountNumber());
                System.out.println("   👤 Titular: " + account.getUserName());
                System.out.println("   📧 Email: " + account.getUserEmail());
                System.out.println("   🔢 CPF: " + account.getUserCpf());
                System.out.println("   💰 Saldo Inicial: R$ " + account.getBalance());
                System.out.println("   📋 Detalhes: " + account.getBankDetails());
                System.out.println("=========================================");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro no processamento: " + e.getMessage());
        }
    }
    
    
    private String extractJsonField(String json, String fieldName) {
        try {
            String searchStr = "\"" + fieldName + "\":";
            int start = json.indexOf(searchStr);
            if (start == -1) return null;
            
            start += searchStr.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) return null;
            
            String value = json.substring(start, end).trim();
            
            // Remove aspas se for string
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            
            return value;
            
        } catch (Exception e) {
            log.debug("Erro ao extrair campo {}: {}", fieldName, e.getMessage());
            return null;
        }
    }
}