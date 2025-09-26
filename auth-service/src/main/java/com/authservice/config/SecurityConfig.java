package com.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // pode desabilitar CSRF se for API REST
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**").permitAll()  // H2 Console liberado
            .requestMatchers("/api/auth/**").permitAll() // Ativação liberada    // Login liberado
            .anyRequest().authenticated()                      // outros endpoints protegidos
        )
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .httpBasic();  // ou JWT, dependendo da sua autenticação

    return http.build();
}

}




