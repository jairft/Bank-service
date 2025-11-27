package com.userservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    private String eventId;
    private String eventType = "USER_UPDATED";
    private LocalDateTime timestamp;
    private String correlationId;
    private Long userId;
    private String cpf;
    private String nome;

    private String email;      // novo email, se houver alteração
    private String telefone;   // novo telefone, se houver alteração

    private LocalDateTime updatedAt;

    public UserUpdatedEvent(Long userId, String cpf, String nome, String email, String telefone, LocalDateTime updatedAt) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "USER_UPDATED";
        this.timestamp = LocalDateTime.now();
        this.correlationId = UUID.randomUUID().toString();
        this.userId = userId;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.updatedAt = updatedAt;
    }
}
