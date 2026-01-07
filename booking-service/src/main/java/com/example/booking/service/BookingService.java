package com.example.booking.service;

import com.example.booking.client.HotelClient;
import com.example.booking.dto.HotelDTO;
import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;

    public Booking createBooking(Booking booking) {
        // Проверка: существует ли отель/номер через Feign запрос в другой сервис
        try {
            HotelDTO hotel = hotelClient.getHotelById(booking.getRoomId());
            System.out.println("Подтверждено: бронируем в отеле " + hotel.getName());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка: Отель с id " + booking.getRoomId() + " не найден!");
        }

        booking.setStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
}