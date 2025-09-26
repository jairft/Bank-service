package com.user_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user_service.dto.CreateUserRequest;
import com.user_service.dto.UserResponse;
import com.user_service.event.UserCreatedEvent;
import com.user_service.model.User;
import com.user_service.repository.UserRepository;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    public UserService(UserRepository userRepository, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Criando novo usuário: {}", request.getEmail());

        // Verifica se usuário já existe
        if (userRepository.existsByCpf(request.getCpf())) {
            throw new RuntimeException("CPF já cadastrado: " + request.getCpf());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + request.getEmail());
        }

        // Cria o usuário
        User user = new User();
        user.setCpf(request.getCpf());
        user.setNome(request.getNome());
        user.setEmail(request.getEmail());
        user.setTelefone(request.getTelefone());

        User savedUser = userRepository.save(user);
        log.info("Usuário criado com ID: {}", savedUser.getId());

        // Publica evento de usuário criado
        try {
            UserCreatedEvent event = new UserCreatedEvent(
                    savedUser.getId(),
                    savedUser.getCpf(),
                    savedUser.getNome(),
                    savedUser.getEmail(),
                    savedUser.getTelefone()
            );
            userEventProducer.sendUserCreatedEvent(event);
            log.info("Evento USER_CREATED publicado para o usuário: {}", savedUser.getId());
        } catch (Exception ex) {
            log.error("Erro ao publicar evento USER_CREATED, mas usuário foi criado. ID: {}", savedUser.getId(), ex);
            // Não lança exceção para não reverter a transação do usuário
        }

        return new UserResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));
        return new UserResponse(user);
    }

    public UserResponse getUserByCpf(String cpf) {
        User user = userRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com CPF: " + cpf));
        return new UserResponse(user);
    }
}
