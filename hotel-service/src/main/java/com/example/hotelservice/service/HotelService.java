package com.example.hotelservice.service;

import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.entity.Room;
import com.example.hotelservice.repository.HotelRepository;
import com.example.hotelservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    // --- ЛОГИКА ДЛЯ КЛИЕНТОВ (Бронирование) ---

    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    @Transactional
    public boolean confirmBooking(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    if (room.isAvailable()) {
                        room.setTimesBooked(room.getTimesBooked() + 1);
                        roomRepository.save(room);
                        log.info("Бронирование подтверждено для комнаты: {}", roomId);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    // --- ЛОГИКА ДЛЯ АДМИНА (Управление отелями) ---

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @Transactional
    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Transactional
    public Hotel updateHotel(Long id, Hotel details) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отель не найден"));
        hotel.setName(details.getName());
        hotel.setLocation(details.getLocation());
        return hotelRepository.save(hotel);
    }

    @Transactional
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    // --- ЛОГИКА ДЛЯ АДМИНА (Управление комнатами) ---

    @Transactional
    public Room addRoom(Long hotelId, Room room) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Отель не найден"));
        room.setHotel(hotel);
        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Long roomId, Room roomDetails) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Комната не найдена"));
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setAvailable(roomDetails.isAvailable());
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        roomRepository.deleteById(roomId);
    }
}