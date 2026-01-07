package com.example.booking.controller;

import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // Регистрация нового пользователя
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        // Шифруем пароль перед сохранением в БД
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }
        userRepository.save(user);
        return "User " + user.getUsername() + " registered successfully!";
    }

    // Вход (Аутентификация) - возвращает JWT токен
    @PostMapping("/auth")
    public Map<String, String> authenticate(@RequestBody User user) {
        User foundUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, совпадает ли введенный пароль с зашифрованным в базе
        if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            String token = jwtUtils.generateToken(foundUser.getUsername());
            // Возвращаем токен в виде JSON
            return Map.of("token", token);
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }
}