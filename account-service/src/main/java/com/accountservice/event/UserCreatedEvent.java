package com.accountservice.event;



import java.time.LocalDateTime;


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
    
    // Construtor simplificado
    public UserCreatedEvent(Long userId, String cpf, String nome, String email, String telefone) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.timestamp = LocalDateTime.now();
        this.dataCadastro = LocalDateTime.now();
        this.correlationId = java.util.UUID.randomUUID().toString();
    }

    public UserCreatedEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    
}