package com.example.booking.repository;

import com.example.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository

// это дает нам методы save(), findById(), delete() и т.д.
public interface UserRepository extends JpaRepository<User, Long> {

    // метод нужен для процесса авторизации (логина)
    Optional<User> findByUsername(String username);
}