package com.accountservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.accountservice.dto.TransactionalPasswordRequest;
import com.accountservice.service.TransactionalPasswordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactional-password")
public class TransactionalPasswordController {
    
    private final TransactionalPasswordService passwordService;
    
    public TransactionalPasswordController(TransactionalPasswordService passwordService) {
        this.passwordService = passwordService;
    }
    
    // ✅ DEFINIR SENHA PELA PRIMEIRA VEZ
    @PostMapping("/set")
    public ResponseEntity<String> setTransactionalPassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransactionalPasswordRequest request) {
        
        passwordService.setTransactionalPassword(userId, request);
        return ResponseEntity.ok("Senha transacional configurada com sucesso");
    }
    
    // ✅ ALTERAR SENHA
    @PostMapping("/change")
    public ResponseEntity<String> changeTransactionalPassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        
        passwordService.changeTransactionalPassword(userId, currentPassword, newPassword);
        return ResponseEntity.ok("Senha transacional alterada com sucesso");
    }
    
    // ✅ VERIFICAR STATUS
    @GetMapping("/status")
    public ResponseEntity<String> getPasswordStatus(@RequestHeader("X-User-Id") Long userId) {
        String status = passwordService.getPasswordStatus(userId);
        return ResponseEntity.ok(status);
    }
}