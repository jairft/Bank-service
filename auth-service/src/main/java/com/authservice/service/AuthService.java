package com.authservice.service;


import com.authservice.dto.*;
import com.authservice.model.UserCredentials;
import com.authservice.model.UserStatus;
import com.authservice.repository.UserCredentialsRepository;
import com.authservice.security.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserCredentialsRepository credentialsRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserCredentialsRepository credentialsRepository, JwtTokenUtil jwtTokenUtil, PasswordEncoder passwordEncoder) {
        this.credentialsRepository = credentialsRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login para: {}", request.getEmail());

        Optional<UserCredentials> credentialsOpt = credentialsRepository.findByEmail(request.getEmail());

        if (credentialsOpt.isEmpty()) {
            log.warn("Credenciais não encontradas para: {}", request.getEmail());
            throw new RuntimeException("Credenciais inválidas");
        }

        UserCredentials credentials = credentialsOpt.get();

        // Verifica status do usuário
        if (credentials.getStatus() != UserStatus.ACTIVE) {
            log.warn("Usuário não está ativo: {}", request.getEmail());
            throw new RuntimeException("Usuário não está ativo");
        }

        // Verifica senha
        if (!passwordEncoder.matches(request.getPassword(), credentials.getPassword())) {
            log.warn("Senha incorreta para: {}", request.getEmail());
            throw new RuntimeException("Credenciais inválidas");
        }

        // Atualiza último login
        credentials.setLastLogin(LocalDateTime.now());
        credentialsRepository.save(credentials);

        // Gera token JWT
        String token = jwtTokenUtil.generateToken(credentials.getEmail(), credentials.getUserId());

        log.info("Login realizado com sucesso para: {}", request.getEmail());

        return new LoginResponse(token, credentials.getUserId(), credentials.getEmail(), "Usuário");
    }

    @Transactional
    public void createInitialPassword(CreatePasswordRequest request) {
        if (!jwtTokenUtil.validateToken(request.getToken())) {
            throw new RuntimeException("Token inválido ou expirado");
        }

        String email = jwtTokenUtil.getEmailFromToken(request.getToken());
        Long userId = jwtTokenUtil.getUserIdFromToken(request.getToken());

        Optional<UserCredentials> credentialsOpt = credentialsRepository.findByUserId(userId);

        if (credentialsOpt.isEmpty()) {
            throw new RuntimeException("Credenciais não encontradas");
        }

        UserCredentials credentials = credentialsOpt.get();

        if (credentials.getStatus() != UserStatus.PENDING_PASSWORD) {
            throw new RuntimeException("Senha já foi definida anteriormente");
        }

        // Define nova senha
        credentials.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credentials.setStatus(UserStatus.ACTIVE);
        credentialsRepository.save(credentials);

        log.info("Senha definida com sucesso para usuário: {}", email);
    }

    public ValidateTokenResponse validateToken(String token) {
        try {
            if (jwtTokenUtil.validateToken(token)) {
                String email = jwtTokenUtil.getEmailFromToken(token);
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                return new ValidateTokenResponse(true, userId, email);
            }
            return new ValidateTokenResponse(false, null, null);
        } catch (Exception ex) {
            log.error("Erro ao validar token: {}", ex.getMessage());
            return new ValidateTokenResponse(false, null, null);
        }
    }

    public String generateSetupToken(String email) {
        Optional<UserCredentials> credentialsOpt = credentialsRepository.findByEmail(email);

        if (credentialsOpt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado");
        }

        UserCredentials credentials = credentialsOpt.get();

        if (credentials.getStatus() != UserStatus.PENDING_PASSWORD) {
            throw new RuntimeException("Senha já foi definida");
        }

        // Gera token para definição de senha (expira em 1 hora)
        return jwtTokenUtil.generateToken(credentials.getEmail(), credentials.getUserId());
    }

    @Transactional
    public ActivationResponse activateAccount(ActivateAccountRequest request) {
        log.info("Tentativa de ativação com token: {}", request.getActivationToken());
        
        // Validações básicas
        if (request.getActivationToken() == null || request.getActivationToken().trim().isEmpty()) {
            throw new RuntimeException("Token de ativação é obrigatório");
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Senhas não coincidem");
        }
        
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Senha deve ter no mínimo 6 caracteres");
        }
        
        // Busca credenciais pelo token
        Optional<UserCredentials> credentialsOpt = credentialsRepository.findByActivationToken(request.getActivationToken());
        
        if (credentialsOpt.isEmpty()) {
            log.warn("Token não encontrado: {}", request.getActivationToken());
            throw new RuntimeException("Token de ativação inválido ou expirado");
        }
        
        UserCredentials credentials = credentialsOpt.get();
        
        // Verifica se o token expirou
        if (credentials.getActivationExpires() != null && 
            credentials.getActivationExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de ativação expirado");
        }
        
        // Verifica se já está ativo
        if (credentials.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Conta já está ativa");
        }
        
        try {
            // Define a senha e ativa a conta
            credentials.setPassword(passwordEncoder.encode(request.getPassword()));
            credentials.setStatus(UserStatus.ACTIVE);
            credentials.setActivationToken(null); // Remove o token
            credentials.setActivationExpires(null);
            credentialsRepository.save(credentials);
            
            log.info("✅ Conta ativada com sucesso para: {}", credentials.getEmail());
            
            return new ActivationResponse("Conta ativada com sucesso", credentials.getEmail(), true);
            
        } catch (Exception e) {
            log.error("Erro ao ativar conta: {}", e.getMessage());
            throw new RuntimeException("Erro interno ao ativar conta");
        }
}

    public UserCredentials getCredentialsByEmail(String email) {
        return credentialsRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciais não encontradas para: " + email));
    }

    public Map<String, Object> getActivationStatus(String email) {
        try {
            UserCredentials credentials = getCredentialsByEmail(email);
            Map<String, Object> status = new HashMap<>();
            status.put("email", credentials.getEmail());
            status.put("status", credentials.getStatus());
            status.put("hasPassword", credentials.getPassword() != null);
            status.put("requiresActivation", credentials.getStatus() == UserStatus.PENDING_ACTIVATION);
            status.put("activationToken", credentials.getActivationToken());
            return status;
        } catch (RuntimeException ex) {
            Map<String, Object> status = new HashMap<>();
            status.put("error", "Usuário não encontrado");
            return status;
        }
    }

    public String resendActivationToken(String email) {
        UserCredentials credentials = credentialsRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (credentials.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Conta já está ativa");
        }

        // Gera novo token
        String newToken = generateActivationToken();
        credentials.setActivationToken(newToken);
        credentials.setActivationExpires(LocalDateTime.now().plusHours(24));
        credentialsRepository.save(credentials);

        log.info("Novo token de ativação gerado para: {}", email);

        return newToken;
    }

    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public String updatePassword(Long userId, UpdatePasswordRequest request) {
        log.info("Atualizando senha para usuário ID: {}", userId);
        
        // Validações
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Nova senha e confirmação não coincidem");
        }
        
        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("Nova senha deve ter no mínimo 6 caracteres");
        }
        
        // Busca o usuário
        UserCredentials credentials = credentialsRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verifica se a conta está ativa
        if (credentials.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Conta não está ativa");
        }
        
        // Verifica a senha atual
        if (!passwordEncoder.matches(request.getCurrentPassword(), credentials.getPassword())) {
            throw new RuntimeException("Senha atual incorreta");
        }
        
        // Verifica se a nova senha é diferente da atual
        if (passwordEncoder.matches(request.getNewPassword(), credentials.getPassword())) {
            throw new RuntimeException("Nova senha deve ser diferente da senha atual");
        }
        
        // Atualiza a senha
        credentials.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credentialsRepository.save(credentials);
        
        log.info("Senha atualizada com sucesso para usuário: {}", credentials.getEmail());
        
        return "Senha atualizada com sucesso";
    }
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        log.info("Redefinindo senha para: {}", request.getEmail());
        
        // Validações
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Nova senha e confirmação não coincidem");
        }
        
        // Busca o usuário
        UserCredentials credentials = credentialsRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verifica se a conta está ativa
        if (credentials.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Conta não está ativa");
        }
        
        // Gera um token de reset temporário (em produção, enviaria por email)
        String resetToken = generateActivationToken();
        credentials.setActivationToken(resetToken);
        credentials.setActivationExpires(LocalDateTime.now().plusHours(1)); // Expira em 1 hora
        
        credentialsRepository.save(credentials);
        
        log.info("Token de redefinição gerado para: {}", request.getEmail());
        log.info("Reset Token: {}", resetToken);
        
        // Em produção, aqui enviaríamos um email com o token
        // Por enquanto, retornamos o token no log
        
        return "Token de redefinição gerado. Verifique os logs do servidor.";
    }

    @Transactional
    public String confirmResetPassword(String resetToken, String newPassword) {
        log.info("Confirmando redefinição com token: {}", resetToken);
        
        // Busca credenciais pelo token de reset
        UserCredentials credentials = credentialsRepository.findByActivationToken(resetToken)
            .orElseThrow(() -> new RuntimeException("Token de redefinição inválido"));
        
        // Verifica se o token expirou
        if (credentials.getActivationExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de redefinição expirado");
        }
        
        // Atualiza a senha
        credentials.setPassword(passwordEncoder.encode(newPassword));
        credentials.setActivationToken(null); // Remove o token
        credentials.setActivationExpires(null);
        credentialsRepository.save(credentials);
        
        log.info("Senha redefinida com sucesso para: {}", credentials.getEmail());
        
        return "Senha redefinida com sucesso";
    }
}