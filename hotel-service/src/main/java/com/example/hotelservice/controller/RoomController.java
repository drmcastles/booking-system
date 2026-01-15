package com.example.hotelservice.controller;

import com.example.hotelservice.entity.Room;
import com.example.hotelservice.entity.BookingReference;
import com.example.hotelservice.repository.BookingReferenceRepository;
import com.example.hotelservice.repository.RoomRepository;
import com.example.hotelservice.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/hotels/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final BookingReferenceRepository bookingRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Room createRoom(@RequestBody Room room) {
        log.info(">>> [HOTEL] Создание новой комнаты: {}", room.getRoomNumber());
        return roomRepository.save(room);
    }

    /**
     * Резервация комнаты.
     */
    @PostMapping("/reserve")
    public void reserveRoom(@RequestBody ReservationRequest request) {
        log.info(">>> [HOTEL] SAGA STEP: Резервация комнаты ID: {} на {} - {}",
                request.getRoomId(), request.getCheckIn(), request.getCheckOut());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        // 1. проверяем доступность (убрал .toLocalDate(), так как там уже LocalDate)
        if (!isRoomAvailable(room, request.getCheckIn(), request.getCheckOut())) {
            log.warn(">>> [HOTEL] ОТКАЗ: Комната {} уже занята!", request.getRoomId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ROOM_ALREADY_OCCUPIED");
        }

        // 2. СОХРАНЯЕМ запись о бронировании
        BookingReference booking = new BookingReference();
        booking.setRoom(room);
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        // 3. Обновляем счетчик
        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);

        log.info(">>> [HOTEL] Резервация успешно зафиксирована");
    }

    @GetMapping("/hotel/{hotelId}/available")
    public List<Long> getAvailableRooms(
            @PathVariable Long hotelId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {

        LocalDate start = LocalDate.parse(checkIn.split("T")[0]);
        LocalDate end = LocalDate.parse(checkOut.split("T")[0]);

        log.info(">>> [HOTEL] Поиск комнат: Отель {}, с {} по {}", hotelId, start, end);

        return roomRepository.findByHotelId(hotelId).stream()
                .filter(room -> isRoomAvailable(room, start, end))
                .sorted(Comparator.comparingInt(Room::getTimesBooked).reversed())
                .map(Room::getId)
                .collect(Collectors.toList());
    }

    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (room.getBookings() == null || room.getBookings().isEmpty()) {
            return true;
        }

        return room.getBookings().stream()
                .filter(b -> !"CANCELLED".equals(b.getStatus()))
                .noneMatch(b -> {
                    // Используем напрямую, так как в сущности тоже должен быть LocalDate
                    return checkIn.isBefore(b.getCheckOut()) && checkOut.isAfter(b.getCheckIn());
                });
    }

    @GetMapping("/debug/rooms")
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
}