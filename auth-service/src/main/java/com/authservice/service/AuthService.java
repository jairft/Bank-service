package com.authservice.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.authservice.dto.ActivateAccountRequest;
import com.authservice.dto.ActivationResponse;
import com.authservice.dto.CreatePasswordRequest;
import com.authservice.dto.LoginRequest;
import com.authservice.dto.LoginResponse;
import com.authservice.dto.ResetPasswordRequest;
import com.authservice.dto.UpdatePasswordRequest;
import com.authservice.dto.ValidateTokenResponse;
import com.authservice.exception.AccountInactiveException;
import com.authservice.exception.InvalidCredentialsException;
import com.authservice.exception.InvalidPasswordException;
import com.authservice.exception.TokenExpiredException;
import com.authservice.exception.UserNotFoundException;
import com.authservice.model.UserCredentials;
import com.authservice.model.UserStatus;
import com.authservice.repository.UserCredentialsRepository;
import com.authservice.security.JwtTokenUtil;

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

    // 🔹 LOGIN
    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login para: {}", request.getEmail());

        UserCredentials credentials = credentialsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email ou senha inválidos"));

        // primeiro valida a senha
        if (!passwordEncoder.matches(request.getPassword(), credentials.getPassword())) {
            throw new InvalidCredentialsException("Email ou senha inválidos");
        }

        // depois verifica status
        if (credentials.getStatus() == UserStatus.PENDING_ACTIVATION) {
            resendActivationToken(request.getEmail());
            // retorna algo que o front pode usar para redirecionar
            return new LoginResponse(null, null, null, "PENDING_ACTIVATION");
        }

        if (credentials.getStatus() == UserStatus.INACTIVE) {
            return new LoginResponse(null, null, null, "INACTIVE");
        }

        // usuário ativo, login normal
        credentials.setLastLogin(LocalDateTime.now());
        credentialsRepository.save(credentials);

        String token = jwtTokenUtil.generateToken(credentials.getEmail(), credentials.getUserId());

        log.info("TOKEN JWT: {}", token);

        log.info("Login realizado com sucesso para: {}", request.getEmail());
        return new LoginResponse(token, credentials.getUserId(), credentials.getEmail(), credentials.getStatus().name());
    }

    // 🔹 CREATE INITIAL PASSWORD
    @Transactional
    public void createInitialPassword(CreatePasswordRequest request) {
        if (!jwtTokenUtil.validateToken(request.getToken())) {
            throw new TokenExpiredException("Token inválido ou expirado");
        }

        Long userId = jwtTokenUtil.getUserIdFromToken(request.getToken());

        UserCredentials credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Credenciais não encontradas"));

        if (credentials.getStatus() != UserStatus.PENDING_PASSWORD) {
            throw new InvalidPasswordException("Senha de acesso já foi definida anteriormente");
        }

        credentials.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credentials.setStatus(UserStatus.ACTIVE);
        credentialsRepository.save(credentials);

        log.info("Senha de acesso definida com sucesso para usuário: {}", credentials.getEmail());
    }

    // 🔹 VALIDATE TOKEN
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

    // 🔹 GENERATE SETUP TOKEN
    public String generateSetupToken(String email) {
        UserCredentials credentials = credentialsRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        if (credentials.getStatus() != UserStatus.PENDING_PASSWORD) {
            throw new InvalidPasswordException("Senha já foi definida");
        }

        return jwtTokenUtil.generateToken(credentials.getEmail(), credentials.getUserId());
    }

    // 🔹 ACTIVATE ACCOUNT
    @Transactional
    public ActivationResponse activateAccount(ActivateAccountRequest request) {
        log.info("Tentativa de ativação com token: {}", request.getActivationToken());

        // Valida token
        if (request.getActivationToken() == null || request.getActivationToken().trim().isEmpty()) {
            log.warn("Token de ativação ausente na requisição");
            throw new InvalidPasswordException("Token de ativação ausente na requisição");
        }

        // Busca credenciais pelo token de ativação
        UserCredentials credentials = credentialsRepository.findByActivationToken(request.getActivationToken())
                .orElseThrow(() -> {
                    log.warn("Token inválido ou expirado: {}", request.getActivationToken());
                    return new InvalidPasswordException("Token ou senha inválidos");
                });

        // Verifica se token expirou
        if (credentials.getActivationExpires() != null && credentials.getActivationExpires().isBefore(LocalDateTime.now())) {
            log.warn("Token expirado para email {}", credentials.getEmail());
            throw new InvalidPasswordException("Token expirado");
        }

        // Verifica se conta já está ativa
        if (credentials.getStatus() == UserStatus.ACTIVE) {
            log.info("Conta já está ativa para email {}", credentials.getEmail());
            throw new InvalidPasswordException("Conta já está ativa");
        }

        // Valida senha
        if (!passwordEncoder.matches(request.getPassword(), credentials.getPassword())) {
            log.warn("Senha informada não confere para email {}", credentials.getEmail());
            throw new InvalidPasswordException("Token ou senha inválidos");
        }

        // Ativa a conta sem alterar a senha
        credentials.setStatus(UserStatus.ACTIVE);
        credentials.setActivationToken(null);
        credentials.setActivationExpires(null);
        credentialsRepository.save(credentials);

        log.info("✅ Conta ativada com sucesso para: {}", credentials.getEmail());
        return new ActivationResponse("Conta ativada com sucesso", credentials.getEmail(), true);
    }


    // 🔹 GET CREDENTIALS
    public UserCredentials getCredentialsByEmail(String email) {
        return credentialsRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Credenciais não encontradas para: " + email));
    }

    // 🔹 GET ACTIVATION STATUS
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
        } catch (UserNotFoundException ex) {
            Map<String, Object> status = new HashMap<>();
            status.put("error", "Usuário não encontrado");
            return status;
        }
    }

    // 🔹 RESEND ACTIVATION TOKEN
    public String resendActivationToken(String email) {
        UserCredentials credentials = credentialsRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        if (credentials.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidPasswordException("Conta já está ativa");
        }

        String newToken = generateActivationToken();
        credentials.setActivationToken(newToken);
        credentials.setActivationExpires(LocalDateTime.now().plusHours(24));
        credentialsRepository.save(credentials);

        log.info("✅ Novo token de ativação gerado para: {}", email);
        log.info("🔐 Token de redefinição gerado: {}", newToken);
        return newToken;
    }

    private String generateActivationToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    // 🔹 UPDATE PASSWORD
    @Transactional
    public String updatePassword(Long userId, UpdatePasswordRequest request) {
        UserCredentials credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        if (credentials.getStatus() != UserStatus.ACTIVE) {
            throw new AccountInactiveException("Conta não está ativa");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidPasswordException("Nova senha de acesso e confirmação não coincidem");
        }

        if (request.getNewPassword().length() < 6) {
            throw new InvalidPasswordException("Nova senha de acesso deve ter no mínimo 6 caracteres");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), credentials.getPassword())) {
            throw new InvalidPasswordException("Senha de acesso atual incorreta");
        }

        if (passwordEncoder.matches(request.getNewPassword(), credentials.getPassword())) {
            throw new InvalidPasswordException("Nova senha de acesso deve ser diferente da senha atual");
        }

        credentials.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credentialsRepository.save(credentials);

        log.info("Senha de acesso atualizada com sucesso para usuário: {}", credentials.getEmail());
        return "Senha de acesso atualizada com sucesso";
    }

    // 🔹 REQUEST PASSWORD RESET
    @Transactional
    public String requestPasswordReset(String email) {
        UserCredentials credentials = credentialsRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        if (credentials.getStatus() != UserStatus.ACTIVE) {
            throw new AccountInactiveException("Conta não está ativa");
        }

        String resetToken = generateActivationToken();
        credentials.setActivationToken(resetToken);
        credentials.setActivationExpires(LocalDateTime.now().plusHours(1));
        credentialsRepository.save(credentials);

        return "Token de redefinição gerado. Verifique seu e-mail.";
    }

    // 🔹 CONFIRM PASSWORD RESET
    @Transactional
    public String confirmPasswordReset(ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new InvalidPasswordException("Token é obrigatório");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidPasswordException("Nova senha e confirmação não coincidem");
        }

        UserCredentials credentials = credentialsRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new TokenExpiredException("Token inválido"));

        if (credentials.getActivationExpires().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token expirado");
        }

        credentials.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credentials.setActivationToken(null);
        credentials.setActivationExpires(null);
        credentialsRepository.save(credentials);

        log.info("✅ Senha redefinida com sucesso para o e-mail: {}", credentials.getEmail());
        return "Senha redefinida com sucesso.";
    }
}
