package com.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TransactionalPasswordRequest {

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 4, max = 4, message = "A senha de transacao deve conter exatamente 4 dígitos")
    @Pattern(regexp = "\\d{4}", message = "A senha de transacao deve conter apenas números")
    private String password;

    @NotBlank(message = "Confirmação é obrigatória")
    @Size(min = 4, max = 4, message = "A confirmação deve conter exatamente 4 dígitos")
    @Pattern(regexp = "\\d{4}", message = "A confirmação deve conter apenas números")
    private String confirmPassword;

    public TransactionalPasswordRequest() {}

    public TransactionalPasswordRequest(String password, String confirmPassword) {
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
