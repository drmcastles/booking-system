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

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    /**
     * РЕАЛИЗАЦИЯ ДВУХШАГОВОЙ СОГЛАСОВАННОСТИ (SAGA)
     */
    @Transactional
    public Booking createBooking(Booking booking) {
        //  Локальное сохранение со статусом PENDING
        // это гарантирует, что у нас есть запись о попытке бронирования
        booking.setStatus("PENDING");
        Booking savedBooking = bookingRepository.save(booking);

        try {
            // Межсервисный запрос к Hotel Service (через Feign Client)
            // пытаемся подтвердить наличие комнаты и увеличить счетчик times_booked
            boolean isAvailable = hotelClient.confirmAvailability(savedBooking.getRoomId());

            if (isAvailable) {
                // ecли отель подтвердил — переводим в финальный статус CONFIRMED
                savedBooking.setStatus("CONFIRMED");
                System.out.println(">>> Успех: Бронирование #" + savedBooking.getId() + " подтверждено отелем.");
            } else {
                // eсли комната занята или не существует
                savedBooking.setStatus("CANCELLED");
                System.out.println(">>> Отказ: Комната недоступна. Статус изменен на CANCELLED.");
            }
        } catch (Exception e) {
            // КОМПЕНСАЦИЯ (Compensating Transaction)
            // если Hotel Service упал, произошел тайм-аут или ошибка сети
            savedBooking.setStatus("CANCELLED");
            System.err.println(">>> ОШИБКА СВЯЗИ: Hotel Service недоступен. Отмена бронирования...");

            // На всякий случай пытаемся «разблокировать» комнату в отеле,
            // если запрос на подтверждение до него все-таки дошел, но ответ потерялся.
            try {
                hotelClient.releaseRoom(savedBooking.getRoomId());
            } catch (Exception ignored) {
                // Отель может быть полностью выключен, поэтому просто игнорируем
            }
        }

        // Финальное сохранение обновленного статуса
        return bookingRepository.save(savedBooking);
    }
}