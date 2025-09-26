package com.authservice.controller;


import com.authservice.dto.*;
import com.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Recebida requisição de login para: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<ActivationResponse> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        log.info("Recebida requisição para ativar conta");
        ActivationResponse response = authService.activateAccount(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-activation/{email}")
    public ResponseEntity<String> resendActivationToken(@PathVariable String email) {
        log.info("Recebida requisição para reenviar token para: {}", email);
        String newToken = authService.resendActivationToken(email);

        // Em produção, aqui enviaria email
        log.info("=== NOVO TOKEN PARA {}: {} ===", email, newToken);

        return ResponseEntity.ok("Token reenviado com sucesso. Verifique o console do servidor.");
    }

    @GetMapping("/check-activation/{email}")
    public ResponseEntity<Map<String, Object>> checkActivationStatus(@PathVariable String email) {
        log.info("Verificando status de ativação para: {}", email);
        Map<String, Object> response = authService.getActivationStatus(email);

        if (response.containsKey("error")) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ValidateTokenResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Recebida requisição para validar token");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(new ValidateTokenResponse(false, null, null));
        }

        String token = authHeader.substring(7);
        ValidateTokenResponse response = authService.validateToken(token);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Erro na autenticação: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    // Endpoint de saúde do serviço
    @GetMapping("health/")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdatePasswordRequest request) {
        
        try {
            // Extrai o token do header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
            }
            
            String token = authHeader.substring(7);
            
            // Valida o token e extrai o userId
            ValidateTokenResponse tokenResponse = authService.validateToken(token);
            if (!tokenResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido ou expirado");
            }
            
            // Atualiza a senha
            String result = authService.updatePassword(tokenResponse.getUserId(), request);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            // Em produção, isso geraria um token e enviaria email
            // Por enquanto, vamos usar o resetPassword diretamente
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setEmail(email);
            request.setNewPassword("temp123"); // Senha temporária
            request.setConfirmNewPassword("temp123");
            
            return ResponseEntity.ok("Solicitação de redefinição processada. Verifique os logs.");
            
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, 
                                            @RequestParam String newPassword) {
        try {
            String result = authService.confirmResetPassword(token, newPassword);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
