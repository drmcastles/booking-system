package com.example.booking.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Ищем заголовок в обоих регистрах
                String authHeader = request.getHeader("Authorization");
                if (authHeader == null) {
                    authHeader = request.getHeader("authorization");
                }

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    // Прокидываем токен дальше
                    requestTemplate.header("Authorization", authHeader);
                    log.info(">>> [FEIGN] Token propagated to: {}", requestTemplate.url());
                } else {
                    log.warn(">>> [FEIGN] No Bearer token found in incoming request to propagate!");
                }
            } else {

                log.warn(">>> [FEIGN] No request context available (async or background task)");
            }
        };
    }
}