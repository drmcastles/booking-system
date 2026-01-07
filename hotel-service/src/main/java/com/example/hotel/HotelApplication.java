package com.example.hotel;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;

@SpringBootApplication
@EnableDiscoveryClient
public class HotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(HotelRepository hotelRepository, RoomRepository roomRepository) {
        return args -> {
            // Очищаем базу перед загрузкой (полезно для тестов)
            roomRepository.deleteAll();
            hotelRepository.deleteAll();

            // 1. Создаем отель
            Hotel hotel = new Hotel();
            hotel.setName("Grand Budapest");
            hotel.setAddress("Moscow, Red Square");
            hotel.setRating(5);
            hotel.setRooms(new ArrayList<>());

            // Сохраняем отель
            Hotel savedHotel = hotelRepository.save(hotel);

            // 2. Создаем комнату 101 (уже популярная)
            Room room1 = new Room();
            room1.setNumber("101");
            room1.setAvailable(true);
            room1.setTimesBooked(5);
            room1.setHotel(savedHotel);

            // 3. Создаем комнату 102 (новая, должна быть первой в рекомендациях по ТЗ)
            Room room2 = new Room();
            room2.setNumber("102");
            room2.setAvailable(true);
            room2.setTimesBooked(0);
            room2.setHotel(savedHotel);

            // Сохраняем комнаты
            roomRepository.save(room1);
            roomRepository.save(room2);

            System.out.println(">>> HOTEL SERVICE: Данные успешно инициализированы.");
            System.out.println(">>> Создан отель: " + savedHotel.getName());
            System.out.println(">>> Созданы комнаты: 101 (5 броней), 102 (0 броней)");
        };
    }
}