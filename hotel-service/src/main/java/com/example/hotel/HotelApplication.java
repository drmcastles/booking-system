package com.example.hotel;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.ArrayList;

@SpringBootApplication
public class HotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(HotelRepository hotelRepository, RoomRepository roomRepository) {
        return args -> {
            roomRepository.deleteAll();
            hotelRepository.deleteAll();

            // Создаем отель (id отеля генерится сам)
            Hotel hotel = new Hotel();
            hotel.setName("Grand Budapest");
            hotel.setAddress("Moscow, Red Square");
            hotel.setRating(5);
            hotel.setRooms(new ArrayList<>());
            Hotel savedHotel = hotelRepository.save(hotel);

            // Создаем комнаты с ВРУЧНУЮ заданными ID
            roomRepository.save(new Room(101L, "101", true, 5, savedHotel));
            roomRepository.save(new Room(102L, "102", true, 0, savedHotel));
            roomRepository.save(new Room(501L, "Suite 501", true, 20, savedHotel));

            System.out.println(">>> HOTEL SERVICE: Успешно запущен. Комнаты 101, 102, 501 созданы.");
        };
    }
}