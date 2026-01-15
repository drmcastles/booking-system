package com.example.bookingservice.controller;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ПРАВА ДОСТУПА

    @Test
    @DisplayName("ADMIN: Видит все бронирования системы")
    @WithMockUser(authorities = "ADMIN")
    void adminSeesEverything() throws Exception {
        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("USER: Ошибка 403 при просмотре всех бронирований")
    @WithMockUser(authorities = "USER")
    void userForbiddenForAllBookings() throws Exception {
        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER: Может править только свою бронь")
// В username передаем строку "123", которую потом сервис сконвертирует в Long
    @WithMockUser(username = "123", authorities = "USER")
    void userModifiesOnlyOwn() throws Exception {
        Booking myBooking = new Booking();
        myBooking.setUserId(123L); // Теперь типы совпадают (Long)
        myBooking.setStatus("PENDING");
        myBooking = bookingRepository.save(myBooking);

        // Используем конструкцию {id} для безопасной передачи Long в путь
        mockMvc.perform(put("/api/bookings/{id}", myBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("USER: Не может править чужую бронь (403)")
    @WithMockUser(username = "123", authorities = "USER")
    void userCannotModifyOthers() throws Exception {
        Booking otherBooking = new Booking();
        otherBooking.setUserId(999L); // Чужой ID
        otherBooking = bookingRepository.save(otherBooking);

        mockMvc.perform(put("/api/bookings/{id}", otherBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isForbidden());
    }

    // ТЕСТ АЛГОРИТМА РЕКОМЕНДАЦИЙ

    @Test
    @DisplayName("RECOMMENDATION: Получение списка рекомендаций")
    @WithMockUser(authorities = "USER")
    void testRecommendationEndpoint() throws Exception {
        mockMvc.perform(get("/api/recommendations/user/ivan_ivanov"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}