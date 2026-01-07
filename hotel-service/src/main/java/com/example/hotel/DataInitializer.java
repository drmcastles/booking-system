package com.example.hotel;

import com.example.hotel.entity.Hotel;
import com.example.hotel.entity.Room;
import com.example.hotel.repository.HotelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final HotelRepository hotelRepository;

    public DataInitializer(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Override
    public void run(String... args) {
        Hotel hotel = new Hotel();
        hotel.setName("Grand Budapest");
        hotel.setAddress("Moscow");
        hotel.setRating(5);

        hotelRepository.save(hotel);
        System.out.println("База данных наполнена тестовым отелем!");
    }
}