package com.api_gateway.filter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
@Order(1)
public class LoggingFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public reactor.core.publisher.Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange,
                                                    org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        log.info("➡️ Request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            log.info("⬅️ Response status: {}", exchange.getResponse().getStatusCode());
        }));
    }
}

