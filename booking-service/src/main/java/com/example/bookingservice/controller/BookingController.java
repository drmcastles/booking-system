package com.example.bookingservice.controller;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import com.example.bookingservice.client.HotelClient;
import com.example.bookingservice.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> payload) {
        log.info(">>> [SAGA] Начало создания брони: {}", payload);
        Booking booking = new Booking();

        try {
            // 1. Извлекаем данные с проверкой на существование
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

            // Сохраняем черновик брони
            Booking savedBooking = bookingRepository.save(booking);
            Long finalRoomId = 0L;

            // 2. ШАГ SAGA: Поиск комнаты (Рекомендация)
            try {
                log.info(">>> [SAGA] Запрос комнат у Hotel Service для отеля {}", hotelId);
                Object response = hotelClient.getAvailableRooms(hotelId, start, end);
                finalRoomId = extractRoomId(response);
            } catch (Exception e) {
                log.error(">>> [SAGA] Hotel Service недоступен при поиске: {}", e.getMessage());
            }

            if (finalRoomId == 0L) {
                savedBooking.setStatus("REJECTED");
                bookingRepository.save(savedBooking);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Нет свободных комнат (SAGA REJECTED)");
            }

            // 3. ШАГ SAGA: Резервация
            try {
                savedBooking.setRoomId(finalRoomId);
                ReservationRequest req = new ReservationRequest(finalRoomId, start.toLocalDate(), end.toLocalDate());
                hotelClient.reserveRoom(req);

                savedBooking.setStatus("CONFIRMED");
                log.info(">>> [SAGA SUCCESS] Бронь подтверждена");
            } catch (Exception e) {
                log.error(">>> [SAGA ROLLBACK] Ошибка резервации: {}", e.getMessage());
                savedBooking.setStatus("REJECTED");
            }

            return ResponseEntity.ok(bookingRepository.save(savedBooking));

        } catch (Exception e) {
            log.error(">>> [CRITICAL] Ошибка: ", e);
            return ResponseEntity.status(500).body("Критическая ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(bookingRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка БД: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public List<Booking> getMy() {
        return bookingRepository.findAll();
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id,
                                           @RequestBody Map<String, String> updates,
                                           Authentication authentication) {
        log.info(">>> Обновление брони ID: {}", id);
        return bookingRepository.findById(id)
                .map(booking -> {
                    // Проверка прав доступа
                    String currentUsername = authentication.getName();
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ADMIN"));

                    // Если не админ и ID владельца не совпадает с username из токена — отказ 403
                    if (!isAdmin && !booking.getUserId().toString().equals(currentUsername)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен: это не ваше бронирование");
                    }

                    if (updates.containsKey("status")) {
                        booking.setStatus(updates.get("status"));
                    }
                    return ResponseEntity.ok(bookingRepository.save(booking));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication authentication) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    // Проверка прав доступа для просмотра
                    String currentUsername = authentication.getName();
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ADMIN"));

                    if (!isAdmin && !booking.getUserId().toString().equals(currentUsername)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен");
                    }

                    return ResponseEntity.ok(booking);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}