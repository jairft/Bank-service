package com.userservice.dto;

import java.time.LocalDateTime;

import com.userservice.model.User;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String cpf;
    private String nome;
    private String email;
    private String telefone;
    
    private LocalDateTime dataCadastro;

    public UserResponse(User user) {
        this.id = user.getId();
        this.cpf = user.getCpf();
        this.nome = user.getNome();
        this.email = user.getEmail();
        this.telefone = user.getTelefone();
        this.dataCadastro = user.getDataCadastro();
    }


    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param cpf the cpf to set
     */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    /**
     * @param nome the nome to set
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @param telefone the telefone to set
     */
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    /**
     * @param dataCadastro the dataCadastro to set
     */
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

}

