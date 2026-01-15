package com.example.hotelservice;

import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.entity.Room;
import com.example.hotelservice.repository.HotelRepository;
import com.example.hotelservice.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HotelServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(HotelRepository hotelRepository, RoomRepository roomRepository) {
        return args -> {
            // Создаем тестовый отель с ID=1
            Hotel hotel = new Hotel();
            hotel.setName("SAGA Plaza");
            hotel.setLocation("Moscow");
            hotel = hotelRepository.save(hotel);

            // Создаем 3 комнаты для этого отеля
            for (int i = 1; i <= 3; i++) {
                Room room = new Room();
                room.setRoomNumber("10" + i);
                room.setType("DELUXE");
                room.setPrice(100.0 * i);
                room.setAvailable(true);
                room.setHotel(hotel);
                room.setTimesBooked(i * 5); // Для теста алгоритма рекомендаций
                roomRepository.save(room);
            }
            System.out.println(">>> [HOTEL SERVICE] Тестовые данные (Отель ID: 1 и комнаты) созданы!");
        };
    }
}