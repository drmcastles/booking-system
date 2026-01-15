package com.example.hotelservice.controller;

import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelRepository hotelRepository;

    @GetMapping("/whoami")
    public String whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "No authentication found in SecurityContext";
        }
        return "User: " + auth.getName() + " | Roles: " + auth.getAuthorities();
    }

    // Метод для получения всех отелей (для тестов и публичного просмотра)
    @GetMapping
    public List<Hotel> getAllHotels() {
        log.info(">>> [HOTEL] Получение списка всех отелей");
        return hotelRepository.findAll();
    }

    // Создание отеля - только для админа
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Hotel createHotel(@RequestBody Hotel hotel) {
        log.info(">>> [HOTEL] Админ создает новый отель: {}", hotel.getName());
        return hotelRepository.save(hotel);
    }

    // Удаление отеля - только для админа
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteHotel(@PathVariable Long id) {
        log.info(">>> [HOTEL] Админ удаляет отель ID: {}", id);
        hotelRepository.deleteById(id);
    }
}