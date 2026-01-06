package com.example.hotel;

import com.example.hotel.model.Hotel;
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
    public CommandLineRunner dataLoader(HotelRepository repo) {
        return args -> {
            repo.save(new Hotel(null, "Radisson Blue", "Москва"));
            repo.save(new Hotel(null, "Azimut", "Санкт-Петербург"));
        };
    }
}