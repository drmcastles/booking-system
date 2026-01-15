package com.example.hotelservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // Хардкодим ТОТ ЖЕ секрет, что в Букинге и Гейтвее
    private final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private Key key;

    @PostConstruct
    public void init() {
        // гарантируем, что берем байты в UTF-8
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateTokenDirectly(String username, String role) {
        String cleanRole = role.replace("ROLE_", "");

        return Jwts.builder()
                .setSubject(username)
                .claim("role", cleanRole)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                // Передаем только key.
                // Библиотека сама поймет, что это HS256, так как ключ создан через hmacShaKeyFor
                .signWith(key)
                .compact();
    }
}