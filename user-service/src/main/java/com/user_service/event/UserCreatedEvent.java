package com.user_service.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String eventId;
    private String eventType = "USER_CREATED";
    private LocalDateTime timestamp = LocalDateTime.now();
    private String correlationId;

    // Dados do usuário
    private Long userId;
    private String cpf;
    private String nome;
    private String email;
    private String telefone;
    private LocalDateTime dataCadastro;

    public UserCreatedEvent(Long userId, String cpf, String nome, String email, String telefone) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataCadastro = LocalDateTime.now();
        this.correlationId = java.util.UUID.randomUUID().toString();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }
}
