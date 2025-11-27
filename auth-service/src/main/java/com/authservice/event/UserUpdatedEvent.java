package com.authservice.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String correlationId;
    private Long userId;
    private String cpf;
    private String nome;
    private String email;
    private String telefone;
    private LocalDateTime updatedAt;
}
