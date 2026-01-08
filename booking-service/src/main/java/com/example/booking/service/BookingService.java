package com.example.booking.service;

import com.example.booking.client.HotelClient;
import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Создание бронирования с использованием SAGA (2-step consistency).
     * Статусы по ТЗ: PENDING, CONFIRMED, CANCELLED.
     */
    @Transactional
    public Booking createBooking(Booking booking) {
        // 1. Валидация входных данных
        validateBookingDates(booking);

        // 2. Шаг 1: Локальная транзакция — сохранение в статусе PENDING
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        log.info(">>> [SAGA START] Бронирование {} сохранено в статусе PENDING", savedBooking.getId());

        try {
            // 3. Шаг 2: Вызов Hotel Service для подтверждения доступности
            log.info(">>> [SAGA STEP] Запрос доступности комнаты {} в Hotel Service", savedBooking.getRoomId());
            boolean isAvailable = hotelClient.confirmAvailability(savedBooking.getRoomId());

            if (isAvailable) {
                // УСПЕХ: Номер свободен
                savedBooking.setStatus("CONFIRMED");
                log.info(">>> [SAGA SUCCESS] Бронирование {} подтверждено (CONFIRMED)", savedBooking.getId());
            } else {
                // ОТКАЗ: Номер занят (по ТЗ переводим в CANCELLED)
                savedBooking.setStatus("CANCELLED");
                log.warn(">>> [SAGA REJECT] Комната занята. Статус изменен на CANCELLED");
            }
        } catch (Exception e) {
            // 4. КОМПЕНСАЦИЯ: Ошибка связи или таймаут
            log.error(">>> [SAGA ERROR] Ошибка внешнего вызова: {}. Выполнение компенсации...", e.getMessage());
            savedBooking.setStatus("CANCELLED");

            try {
                // Вызов метода release, если на стороне Hotel Service могла остаться блокировка
                hotelClient.releaseRoom(savedBooking.getRoomId());
                log.info(">>> [COMPENSATION] Слот для комнаты {} успешно освобожден", savedBooking.getRoomId());
            } catch (Exception releaseEx) {
                log.error(">>> [COMPENSATION FAIL] Не удалось вызвать release: {}", releaseEx.getMessage());
            }
        }

        // Сохраняем финальный статус (CONFIRMED или CANCELLED)
        return bookingRepository.save(savedBooking);
    }

    private void validateBookingDates(Booking booking) {
        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            throw new IllegalArgumentException("Даты не могут быть пустыми");
        }
        if (booking.getStartDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата заезда не может быть в прошлом");
        }
        if (booking.getStartDate().isAfter(booking.getEndDate())) {
            throw new IllegalArgumentException("Дата заезда должна быть раньше даты выезда");
        }
    }
}