package com.example.hotelservice.service;

import com.example.hotelservice.repository.UserRepository;
import com.example.hotelservice.security.JwtUtils;
import com.example.hotelservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public String authenticateUser(String username, String password) {
        log.info(">>> [HOTEL-AUTH] Вход для: {}", username);

        // 1. Ищем юзера напрямую в базе
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Проверяем пароль (совпадает ли сырой с тем, что в базе)
        if (passwordEncoder.matches(password, user.getPassword()) || password.equals(user.getPassword())) {
            log.info(">>> [HOTEL-AUTH] Пароль подошел!");

            // 3. Генерируем токен вручную, чтобы не падать на приведении типов Authentication
            return jwtUtils.generateTokenDirectly(user.getUsername(), user.getRole());
        } else {
            log.error(">>> [HOTEL-AUTH] Неверный пароль!");
            throw new RuntimeException("Invalid password");
        }
    }
}