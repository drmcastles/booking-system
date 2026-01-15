package com.example.bookingservice.service;

import com.example.bookingservice.dto.CreateBookingRequest;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;

    public Booking createBooking(CreateBookingRequest request) {
        log.info(">>> [SAGA START] Создание брони. Отель: {}, Юзер: {}", request.getHotelId(), request.getUserId());

        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setHotelId(request.getHotelId());
        booking.setStart(request.getCheckIn());
        booking.setEnd(request.getCheckOut());
        booking.setStatus("PENDING");
        booking = bookingRepository.save(booking);

        try {
            // URL соответствует маппингу в Hotel Service
            String hotelServiceUrl = "http://hotel-service/api/hotels/rooms/hotel/"
                    + request.getHotelId() + "/available?checkIn=" + request.getCheckIn()
                    + "&checkOut=" + request.getCheckOut();

            log.info(">>> [SAGA STEP] Запрос в Hotel Service: {}", hotelServiceUrl);

            // Получаем список ID комнат
            List<?> availableRoomIds = restTemplate.getForObject(hotelServiceUrl, List.class);

            if (availableRoomIds == null || availableRoomIds.isEmpty()) {
                throw new RuntimeException("Нет доступных комнат на указанные даты");
            }

            // РЕКОМЕНДАЦИЯ: Берем первый ID из списка (он уже отсортирован по популярности)
            Long recommendedRoomId = Long.valueOf(availableRoomIds.get(0).toString());

            booking.setRoomId(recommendedRoomId);
            booking.setStatus("CONFIRMED");
            log.info(">>> [SAGA DONE] Бронь подтверждена. Выбрана комната ID: {}", recommendedRoomId);

        } catch (Exception e) {
            log.error(">>> [SAGA REJECT] Отмена. Причина: {}", e.getMessage());
            booking.setStatus("REJECTED");
        }

        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        log.info(">>> [CRUD] Удаление брони ID: {}", id);
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Бронирование не найдено");
        }
        bookingRepository.deleteById(id);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking updateBooking(Long id, Booking details) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        booking.setStatus(details.getStatus());
        booking.setStart(details.getStart());
        booking.setEnd(details.getEnd());
        return bookingRepository.save(booking);
    }
}