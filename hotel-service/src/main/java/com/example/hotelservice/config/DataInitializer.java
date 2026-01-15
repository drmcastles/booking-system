package com.example.hotelservice.config;

import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.entity.Room;
import com.example.hotelservice.repository.HotelRepository;
import com.example.hotelservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (hotelRepository.count() == 0) {
            // 1. Создаем тестовый отель
            Hotel hotel = new Hotel();
            hotel.setName("Grand Budapest");
            hotel.setLocation("Alps");

            // сохраняем отель и используем возвращенный объект с ID
            hotel = hotelRepository.save(hotel);

            // 2. Создаем комнаты, передавая уже сохраненный отель
            createRoom(hotel, "101", "Standard", 10);
            createRoom(hotel, "102", "Luxury", 2);
            createRoom(hotel, "103", "Standard", 5);

            System.out.println(">>> [HOTEL] Тестовый отель и комнаты созданы");
        }
    }

    private void createRoom(Hotel hotel, String num, String type, int booked) {
        Room r = new Room();
        r.setHotel(hotel);
        r.setRoomNumber(num);
        r.setType(type);
        r.setTimesBooked(booked);
        r.setPrice(100.0);
        r.setAvailable(true);
        roomRepository.save(r);
    }
}