package com.example.bookingservice;

import com.example.bookingservice.client.HotelClient;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingSagaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private HotelClient hotelClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Проверка SAGA: Откат в REJECTED, если отель не найден")
    @WithMockUser(authorities = "ADMIN")
    void testSagaRollback() throws Exception {
        // Симулируем пустой ответ от отеля (комнат нет)
        Mockito.when(hotelClient.getAvailableRooms(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("hotelId", 999);
        payload.put("startDate", "2026-05-01T10:00:00");
        payload.put("endDate", "2026-05-10T10:00:00");

        mockMvc.perform(post("/api/bookings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());

        // Проверяем, что в БД запись сохранилась со статусом REJECTED
        Booking booking = bookingRepository.findAll().stream()
                .filter(b -> b.getHotelId() == 999L)
                .findFirst()
                .orElseThrow();

        assertEquals("REJECTED", booking.getStatus(), "SAGA должна была перевести бронь в REJECTED");
    }
}