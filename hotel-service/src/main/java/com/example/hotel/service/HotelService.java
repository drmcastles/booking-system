package com.example.hotel.service;

import com.example.hotel.entity.Room;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.repository.HotelRepository; // Добавили репозиторий отелей
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository; // Нужен для привязки комнаты к отелю

    // 1. Для клиентов: список рекомендованных (сортировка по популярности)
    public List<Room> getRecommendedRooms() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedDescIdAsc();
    }

    // 2. Для клиентов: просто все свободные
    public List<Room> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    // 3. SAGA: Подтверждение бронирования (шаг 2)
    @Transactional
    public boolean confirmAvailability(Long roomId, LocalDate startDate, LocalDate endDate) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    if (room.isAvailable()) {
                        room.setTimesBooked(room.getTimesBooked() + 1);
                        // room.setAvailable(false); // Можно раскомментировать, если хочешь жестко блокировать
                        roomRepository.save(room);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    // 4. SAGA: Отмена бронирования (компенсация)
    @Transactional
    public void releaseRoom(Long roomId) {
        roomRepository.findById(roomId).ifPresent(room -> {
            if (room.getTimesBooked() > 0) {
                room.setTimesBooked(room.getTimesBooked() - 1);
                // room.setAvailable(true); // Если блокировал в confirm, тут разблокируй
                roomRepository.save(room);
            }
        });
    }

    // --- НОВОЕ: Методы для ADMIN (чтобы RoomController работал) ---

    // 5. Создание/обновление комнаты с привязкой к отелю
    @Transactional
    public Room saveRoom(Long hotelId, Room room) {
        return hotelRepository.findById(hotelId).map(hotel -> {
            room.setHotel(hotel);
            return roomRepository.save(room);
        }).orElseThrow(() -> new RuntimeException("Отель с ID " + hotelId + " не найден"));
    }

    // 6. Удаление комнаты
    @Transactional
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}