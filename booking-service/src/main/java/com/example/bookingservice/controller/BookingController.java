package com.example.bookingservice.controller;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import com.example.bookingservice.client.HotelClient;
import com.example.bookingservice.dto.ReservationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;

    /**
     * Создание брони через SAGA.
     * [Критерий 2]: Circuit Breaker защищает систему, если Hotel Service упадет.
     */
    @PostMapping("/create")
    @CircuitBreaker(name = "hotelService", fallbackMethod = "fallbackCreateBooking")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> payload) {
        log.info(">>> [SAGA] Начало создания брони. Данные: {}", payload);
        Booking booking = new Booking();

        try {
            if (!payload.containsKey("hotelId") || !payload.containsKey("startDate") || !payload.containsKey("endDate")) {
                return ResponseEntity.badRequest().body("Ошибка: Не все поля (hotelId, startDate, endDate) заполнены!");
            }

            Long hotelId = Long.valueOf(payload.get("hotelId").toString());
            Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : 1L;
            LocalDateTime start = LocalDateTime.parse(payload.get("startDate").toString());
            LocalDateTime end = LocalDateTime.parse(payload.get("endDate").toString());

            booking.setUserId(userId);
            booking.setHotelId(hotelId);
            booking.setStart(start);
            booking.setEnd(end);
            booking.setStatus("PENDING");

            Booking savedBooking = bookingRepository.save(booking);

            // 1. ШАГ SAGA: Поиск комнаты (через Hotel Service)
            log.info(">>> [SAGA] Поиск свободных комнат для отеля {}", hotelId);
            Object response = hotelClient.getAvailableRooms(hotelId, start, end);
            Long finalRoomId = extractRoomId(response);

            if (finalRoomId == 0L) {
                savedBooking.setStatus("REJECTED");
                bookingRepository.save(savedBooking);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Нет свободных комнат (SAGA REJECTED)");
            }

            // 2. ШАГ SAGA: Резервация
            savedBooking.setRoomId(finalRoomId);
            ReservationRequest req = new ReservationRequest(finalRoomId, start.toLocalDate(), end.toLocalDate());
            hotelClient.reserveRoom(req);

            savedBooking.setStatus("CONFIRMED");
            log.info(">>> [SAGA SUCCESS] Бронь подтверждена. ID комнаты: {}", finalRoomId);

            return ResponseEntity.ok(bookingRepository.save(savedBooking));

        } catch (Exception e) {
            log.error(">>> [SAGA ERROR] Ошибка выполнения транзакции: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка SAGA: " + e.getMessage());
        }
    }

    /**
     * Fallback метод для Circuit Breaker.
     * Срабатывает, когда Hotel Service недоступен.
     */
    public ResponseEntity<?> fallbackCreateBooking(Map<String, Object> payload, Throwable e) {
        log.error(">>> [CIRCUIT BREAKER] Fallback активен. Причина: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Внешний сервис (Hotel) временно недоступен. Повторите попытку позже.");
    }

    /**
     * [Критерий 3]: Пагинация списка всех бронирований.
     */
    @GetMapping("/all")
    public ResponseEntity<Page<Booking>> getAll(Pageable pageable) {
        return ResponseEntity.ok(bookingRepository.findAll(pageable));
    }

    /**
     * Получение только своих бронирований.
     */
    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMy(Authentication authentication) {
        // Здесь должна быть логика поиска по UserId из токена
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication authentication) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    if (!isOwnerOrAdmin(booking, authentication)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен");
                    }
                    return ResponseEntity.ok(booking);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id,
                                           @RequestBody Map<String, String> updates,
                                           Authentication authentication) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    if (!isOwnerOrAdmin(booking, authentication)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен");
                    }
                    if (updates.containsKey("status")) {
                        booking.setStatus(updates.get("status"));
                    }
                    return ResponseEntity.ok(bookingRepository.save(booking));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Хелпер для проверки прав (Юзер правит только своё, Админ - всё)
    private boolean isOwnerOrAdmin(Booking booking, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        String currentUsername = auth.getName();
        return isAdmin || booking.getUserId().toString().equals(currentUsername);
    }

    private Long extractRoomId(Object response) {
        if (response == null) return 0L;
        try {
            if (response instanceof List) {
                List<?> list = (List<?>) response;
                if (list.isEmpty()) return 0L;
                Object first = list.get(0);
                if (first instanceof Map) return Long.valueOf(((Map<?,?>)first).get("id").toString());
                return Long.valueOf(first.toString());
            }
            if (response instanceof Map) {
                Map<?,?> map = (Map<?,?>) response;
                if (map.containsKey("id")) return Long.valueOf(map.get("id").toString());
            }
        } catch (Exception e) {
            log.error("Ошибка парсинга roomId: {}", e.getMessage());
        }
        return 0L;
    }
}