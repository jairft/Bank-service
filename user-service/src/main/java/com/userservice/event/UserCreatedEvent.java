package com.userservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String eventId;
    private String eventType = "USER_CREATED";
    private LocalDateTime timestamp;
    private String correlationId;
    private Long userId;
    private String cpf;
    private String nome;
    private String email;
    private String telefone;
    private String password;
    private LocalDateTime dataCadastro;
    

    public UserCreatedEvent(Long userId, String cpf, String nome, String email, String telefone,
                        LocalDateTime dataCadastro, String password) {
    this.eventId = UUID.randomUUID().toString();
    this.eventType = "USER_CREATED";
    this.timestamp = LocalDateTime.now();
    this.correlationId = UUID.randomUUID().toString();
    this.userId = userId;
    this.cpf = cpf;
    this.nome = nome;
    this.email = email;
    this.telefone = telefone;
    this.dataCadastro = dataCadastro;
    this.password = password;
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
