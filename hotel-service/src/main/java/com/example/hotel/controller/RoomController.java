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

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findByAvailableTrue();
    }

    // Рекомендации: самые бронируемые комнаты сверху
    @GetMapping("/recommend")
    public List<Room> getRecommended() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedDescIdAsc();
    }

    @PostMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> addRoom(@PathVariable Long hotelId, @RequestBody Room room) {
        return hotelRepository.findById(hotelId).map(hotel -> {
            room.setHotel(hotel);
            room.setAvailable(true); // При создании комната доступна
            room.setTimesBooked(0);
            return ResponseEntity.ok(roomRepository.save(room));
        }).orElse(ResponseEntity.notFound().build());
    }

    
    @PostMapping("/{id}/confirm-availability")
    public boolean confirmAvailability(@PathVariable Long id) {
        log.info(">>> [SAGA] Проверка подтверждения для комнаты ID: {}", id);
        return roomRepository.findById(id).map(room -> {
            // Если админ вручную выключил комнату (available=false), вернем false
            if (!room.isAvailable()) {
                log.warn(">>> Комната {} выведена из эксплуатации", id);
                return false;
            }

            // Увеличиваем счетчик бронирований для алгоритма рекомендаций
            room.setTimesBooked(room.getTimesBooked() + 1);
            roomRepository.save(room);

            log.info(">>> Комната {} успешно подтверждена", id);
            return true;
        }).orElse(false); // Комната не найдена
    }

    /**
     * SAGA: Отмена (Компенсация).
     * Если бронирование сорвалось на этапе оплаты или валидации, откатываем счетчик.
     */
    @PostMapping("/{id}/release")
    public void releaseRoom(@PathVariable Long id) {
        log.info(">>> [SAGA] Компенсация: откат брони для комнаты ID: {}", id);
        roomRepository.findById(id).ifPresent(room -> {
            room.setTimesBooked(Math.max(0, room.getTimesBooked() - 1));
            roomRepository.save(room);
        });
    }
}