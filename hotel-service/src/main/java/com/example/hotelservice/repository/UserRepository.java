package com.example.hotelservice.repository;

import com.example.hotelservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Не забудь импорт

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring сам сгенерирует SQL запрос: SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);
}