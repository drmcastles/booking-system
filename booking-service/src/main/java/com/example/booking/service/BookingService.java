package com.example.booking.service;

import com.example.booking.client.HotelClient;
import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        booking.setStatus("PENDING");
        Booking savedBooking = bookingRepository.save(booking);

        System.out.println(">>> SAGA STEP 1: Бронирование сохранено как PENDING. ID: " + savedBooking.getId());

        try {
            System.out.println(">>> SAGA STEP 2: Запрос к Hotel Service для комнаты: " + savedBooking.getRoomId());
            boolean isAvailable = hotelClient.confirmAvailability(savedBooking.getRoomId());

            if (isAvailable) {
                savedBooking.setStatus("CONFIRMED");
                System.out.println(">>> SAGA STEP 3: Отель подтвердил наличие. Статус: CONFIRMED");
            } else {
                savedBooking.setStatus("CANCELLED");
                System.out.println(">>> SAGA STEP 3: Отель отказал (мест нет). Статус: CANCELLED");
            }
        } catch (Exception e) {
            System.err.println(">>> SAGA ERROR: Ошибка при связи с Hotel Service!");
            System.err.println(">>> Причина: " + e.getMessage()); // Это самое важное сообщение в консоли

            savedBooking.setStatus("CANCELLED");
            try {
                hotelClient.releaseRoom(savedBooking.getRoomId());
            } catch (Exception ignored) {}
        }

        return bookingRepository.save(savedBooking);
    }
}