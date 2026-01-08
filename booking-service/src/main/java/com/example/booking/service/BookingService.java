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
     * Создание бронирования с проверкой на double-booking и SAGA-транзакцией.
     */
    @Transactional
    public Booking createBooking(Booking booking) {
        // 1. Валидация корректности дат (не в прошлом, конец после начала)
        validateBookingDates(booking);

        // 2. Проверка на повторное бронирование (пересечение дат)
        checkAvailability(booking);

        // 3. Шаг 1 SAGA: Сохранение в локальную БД со статусом PENDING
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        log.info(">>> [SAGA START] Бронирование {} создано в статусе PENDING", savedBooking.getId());

        try {
            // 4. Шаг 2 SAGA: Внешний вызов Hotel Service через Feign
            log.info(">>> [SAGA STEP] Запрос подтверждения комнаты {} в Hotel Service", savedBooking.getRoomId());
            boolean isAvailable = hotelClient.confirmAvailability(savedBooking.getRoomId());

            if (isAvailable) {
                // УСПЕХ: Номер подтвержден отелем
                savedBooking.setStatus("CONFIRMED");
                log.info(">>> [SAGA SUCCESS] Бронирование {} подтверждено", savedBooking.getId());
            } else {
                // ОТКАЗ: Отель сообщил, что номер занят
                savedBooking.setStatus("CANCELLED");
                log.warn(">>> [SAGA REJECT] Hotel Service отклонил бронь для комнаты {}", savedBooking.getRoomId());
            }
        } catch (Exception e) {
            // 5. КОМПЕНСАЦИЯ: Ошибка сети, таймаут или 403
            log.error(">>> [SAGA ERROR] Ошибка связи: {}. Отмена бронирования...", e.getMessage());
            savedBooking.setStatus("CANCELLED");

            try {
                // Пытаемся гарантированно освободить слот, если отель успел его заблочить
                hotelClient.releaseRoom(savedBooking.getRoomId());
            } catch (Exception releaseEx) {
                log.error(">>> [COMPENSATION FAIL] Не удалось вызвать releaseRoom: {}", releaseEx.getMessage());
            }
        }

        // Сохраняем финальное состояние
        return bookingRepository.save(savedBooking);
    }

    /**
     * Логика проверки пересечения интервалов дат.
     */
    private void checkAvailability(Booking newBooking) {
        List<Booking> activeBookings = bookingRepository.findByRoomIdAndStatusIn(
                newBooking.getRoomId(), List.of("PENDING", "CONFIRMED"));

        // Формула пересечения: (StartA < EndB) AND (EndA > StartB)
        boolean isOverlapping = activeBookings.stream().anyMatch(existing ->
                newBooking.getStartDate().isBefore(existing.getEndDate()) &&
                        newBooking.getEndDate().isAfter(existing.getStartDate())
        );

        if (isOverlapping) {
            log.warn(">>> [CONFLICT] Обнаружено пересечение дат для комнаты {}", newBooking.getRoomId());
            throw new IllegalStateException("Этот номер уже забронирован на указанный период");
        }
    }

    private void validateBookingDates(Booking booking) {
        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            throw new IllegalArgumentException("Даты заезда и выезда обязательны");
        }
        if (booking.getStartDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя забронировать номер на прошедшую дату");
        }
        if (!booking.getStartDate().isBefore(booking.getEndDate())) {
            throw new IllegalArgumentException("Дата выезда должна быть позже даты заезда");
        }
    }
}