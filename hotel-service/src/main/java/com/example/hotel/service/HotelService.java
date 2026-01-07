package com.example.hotel.service;

import com.example.hotel.entity.Room;
import com.example.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final RoomRepository roomRepository;

    // метод для получения рекомендованных комнат (сортировка по популярности)
    public List<Room> getRecommendedRooms() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedAscIdAsc();
    }

    // метод подтверждения бронирования (
    @Transactional
    public boolean confirmAvailability(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    if (room.isAvailable()) {
                        room.setTimesBooked(room.getTimesBooked() + 1);
                        roomRepository.save(room);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    // метод отмены/компенсации
    @Transactional
    public void releaseRoom(Long roomId) {
        roomRepository.findById(roomId).ifPresent(room -> {
            if (room.getTimesBooked() > 0) {
                room.setTimesBooked(room.getTimesBooked() - 1);
                roomRepository.save(room);
            }
        });
    }
}