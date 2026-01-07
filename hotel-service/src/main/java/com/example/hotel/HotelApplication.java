package com.example.hotel;

import com.example.hotel.entity.Hotel;
import com.example.hotel.repository.HotelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class HotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(HotelRepository hotelRepository) {
        return args -> {
            // Создаем отель через пустой конструктор и сеттеры (самый надежный путь)
            Hotel hotel = new Hotel();
            hotel.setName("Grand Budapest");
            hotel.setAddress("Moscow, Red Square");
            hotel.setRating(5);

            // Сохраняем в базу H2
            hotelRepository.save(hotel);

            System.out.println(">>> Тестовые данные для Hotel Service успешно загружены!");
        };
    }
}