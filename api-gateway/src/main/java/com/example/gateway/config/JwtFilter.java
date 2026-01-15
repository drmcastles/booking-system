package com.example.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    // Тот же секрет, что и в остальных сервисах
    private final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    public JwtFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Заголовок Authorization отсутствует");
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный формат токена");
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Явная проверка срока действия (на случай, если парсер не выкинул Exception)
                if (claims.getExpiration().before(new Date())) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Срок действия токена истек");
                }

                // Можно добавить логин в хедер для следующих сервисов (опционально)
                exchange.getRequest().mutate()
                        .header("X-User-Name", claims.getSubject())
                        .header("X-User-Role", claims.get("role", String.class))
                        .build();

            } catch (Exception e) {
                // Если подпись не совпала или токен протух - летит 401
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Доступ запрещен: " + e.getMessage());
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {}
}