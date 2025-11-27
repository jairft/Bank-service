package com.userservice.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String telefone;
}
