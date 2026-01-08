package com.example.hotel.controller;

import com.example.hotel.entity.Hotel;
import com.example.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelRepository hotelRepository;

    // Получить список всех отелей (для всех)
    @GetMapping
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    // Создать отель (только ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Hotel createHotel(@RequestBody Hotel hotel) {
        log.info(">>> ADMIN: Регистрация нового отеля: {}", hotel.getName());
        return hotelRepository.save(hotel);
    }

    // Удалить отель (только ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}