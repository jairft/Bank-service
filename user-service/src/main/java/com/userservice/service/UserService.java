package com.userservice.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userservice.dto.CreateUserRequest;
import com.userservice.dto.UpdateUserRequest;
import com.userservice.dto.UserResponse;
import com.userservice.event.UserCreatedEvent;
import com.userservice.event.UserUpdatedEvent;
import com.userservice.exception.UserNotFoundException;
import com.userservice.model.User;
import com.userservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                       UserEventProducer userEventProducer,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Criando novo usuário: {}", request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password e confirmação não coincidem");
        }

        if (userRepository.existsByCpf(request.getCpf())) {
            throw new RuntimeException("CPF já cadastrado: " + request.getCpf());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + request.getEmail());
        }

        

        User user = new User();
        user.setCpf(request.getCpf());
        user.setNome(request.getNome());
        user.setEmail(request.getEmail());
        user.setTelefone(request.getTelefone());
        user.setDataCadastro(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("Usuário criado com ID: {}", savedUser.getId());

        // 🔹 Envia senha apenas para AuthService via Kafka
        UserCreatedEvent event = new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getCpf(),
            savedUser.getNome(),
            savedUser.getEmail(),
            savedUser.getTelefone(),
            savedUser.getDataCadastro(),
            request.getPassword() // senha NÃO vai para o User
        );

        userEventProducer.sendUserCreatedEvent(event);
        log.info("Evento enviado para AuthService para criação de credenciais");

        return new UserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("Atualizando usuário com ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + userId));

        boolean updated = false;

        String updatedEmail = null;
        String updatedTelefone = null;

        // Atualiza apenas se houver alteração
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email já cadastrado: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
            updatedEmail = request.getEmail();  // atribui novo email para o evento
            updated = true;
        }

        if (request.getTelefone() != null && !request.getTelefone().equals(user.getTelefone())) {
            user.setTelefone(request.getTelefone());
            updatedTelefone = request.getTelefone(); // atribui novo telefone para o evento
            updated = true;
        }

        if (!updated) {
            log.info("Nenhuma alteração detectada para o usuário {}", userId);
            return new UserResponse(user);
        }

        userRepository.save(user);
        log.info("Usuário atualizado com sucesso: {}", userId);

        // 🔹 Envia evento de atualização
        UserUpdatedEvent event = new UserUpdatedEvent(
            user.getId(),
            user.getCpf(),
            user.getNome(),
            updatedEmail,       // null se não houve alteração
            updatedTelefone,    // null se não houve alteração
            LocalDateTime.now()
        );

        userEventProducer.sendUserUpdatedEvent(event);
        log.info("Evento USER_UPDATED enviado para outros microsserviços");

        return new UserResponse(user);
    }


    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + id));
        return new UserResponse(user);
    }

    public UserResponse getUserByCpf(String cpf) {
        User user = userRepository.findByCpf(cpf)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com CPF: " + cpf));
        return new UserResponse(user);
    }
}
