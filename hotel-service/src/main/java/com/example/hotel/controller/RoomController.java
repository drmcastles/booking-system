package com.example.hotel.controller;

import com.example.hotel.entity.Room;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    // 1. Получить все доступные комнаты
    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findByAvailableTrue();
    }

    // 2. ТЗ: Алгоритм рекомендаций (сортировка по популярности)
    @GetMapping("/recommend")
    public List<Room> getRecommended() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedAscIdAsc();
    }

    // 3. Создать комнату и привязать к отелю (ADMIN)
    @PostMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> addRoom(@PathVariable Long hotelId, @RequestBody Room room) {
        return hotelRepository.findById(hotelId).map(hotel -> {
            room.setHotel(hotel);
            return ResponseEntity.ok(roomRepository.save(room));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. Удалить комнату (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 5. МЕТОД ДЛЯ SAGA: Подтверждение бронирования
    // Теперь комната помечается как ЗАНЯТАЯ (available = false)
    @PostMapping("/{id}/confirm-availability")
    public ResponseEntity<Boolean> confirmAvailability(@PathVariable Long id) {
        log.info(">>> SAGA: Проверка доступности комнаты ID: {}", id);
        return roomRepository.findById(id).map(room -> {
            if (!room.isAvailable()) {
                log.warn(">>> SAGA: Комната {} уже занята", id);
                return ResponseEntity.ok(false);
            }

            // ОБНОВЛЕНИЕ СОСТОЯНИЯ
            room.setAvailable(false); // Блокируем комнату
            room.setTimesBooked(room.getTimesBooked() + 1); // Повышаем популярность

            roomRepository.save(room);
            log.info(">>> SAGA: Комната {} УСПЕШНО забронирована", id);
            return ResponseEntity.ok(true);
        }).orElse(ResponseEntity.ok(false));
    }

    // 6. МЕТОД ДЛЯ SAGA: Отмена/Освобождение (Компенсация)
    // Возвращает комнату в статус ДОСТУПНА (available = true)
    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseRoom(@PathVariable Long id) {
        log.info(">>> SAGA: Компенсация. Освобождение комнаты ID: {}", id);
        roomRepository.findById(id).ifPresent(room -> {
            room.setAvailable(true); // Снова открываем для бронирования
            room.setTimesBooked(Math.max(0, room.getTimesBooked() - 1)); // Откатываем счетчик

            roomRepository.save(room);
            log.info(">>> SAGA: Комната {} снова ДОСТУПНА", id);
        });
        return ResponseEntity.ok().build();
    }
}