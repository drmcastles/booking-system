package com.example.booking.controller;

import com.example.booking.dto.RoomDto;
import com.example.booking.entity.Booking;
import com.example.booking.service.BookingService;
import com.example.booking.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final RecommendationService recommendationService;

    // Создание бронирования (SAGA внутри)
    @PostMapping
    public ResponseEntity<Booking> create(@RequestBody Booking booking) {
        return ResponseEntity.ok(bookingService.createBooking(booking));
    }

    // Получение всех броней
    @GetMapping
    public ResponseEntity<List<Booking>> getAll() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Тот самый эндпоинт для Алгоритма Рекомендаций
    @GetMapping("/recommendations")
    public ResponseEntity<List<RoomDto>> getRecommendations() {
        List<RoomDto> recommendations = recommendationService.getRecommendations();
        return ResponseEntity.ok(recommendations);
    }
}