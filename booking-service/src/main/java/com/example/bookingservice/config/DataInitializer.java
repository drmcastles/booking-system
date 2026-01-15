package com.example.bookingservice.config;

import com.example.bookingservice.entity.User;
import com.example.bookingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) {
        userRepository.deleteAll();

        // 1. Админ (логин: admin, пароль: password)
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);

        // 2. Тестовый юзер с ID=1
        User testUser = new User();
        testUser.setUsername("user1");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole("ROLE_USER");
        userRepository.save(testUser);

        System.out.println("----------------------------------------------");
        System.out.println(">>> [INIT] Юзеры созданы. Попробуй логин 'admin' / 'password'");
        System.out.println("----------------------------------------------");
    }
}