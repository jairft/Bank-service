package com.api_gateway.filter;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
@Order(2)
public class JwtAuthenticationFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Rotas públicas exatas
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/api/auth/login",
        "/api/auth/check-status",
        "/api/auth/activate",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/users/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Normaliza barra final
        String normalizedPath = path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 1)
                : path;

        log.debug("🔹 Requisição recebida: {}", normalizedPath);

        // Verifica se é rota pública
        boolean isPublic = PUBLIC_PATHS.contains(normalizedPath)
                || normalizedPath.startsWith("/api/auth/resend-activation/")
                || normalizedPath.startsWith("/api/auth/check-activation/")
                || normalizedPath.matches("/api/accounts/.*/deposit");

        if (isPublic) {
            log.debug("🔓 Acesso público permitido para {}", normalizedPath);
            return chain.filter(exchange);
        }

        // Header Authorization obrigatório
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("⚠️ Token ausente ou inválido no header: {}", normalizedPath);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String token = authHeader.substring(7);
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info("✅ Token válido - userId: {}, role: {}", 
                     claims.getSubject(), claims.get("role"));

            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("❌ Erro ao validar JWT: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
