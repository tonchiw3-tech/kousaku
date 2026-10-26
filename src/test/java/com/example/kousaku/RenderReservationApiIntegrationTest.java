package com.example.kousaku;

import com.example.kousaku.mapper.ReservationMapper;
import com.example.kousaku.model.Reservation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("render")
class RenderReservationApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ReservationMapper reservationMapper;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void createWithRenderProfileReturns201AndPersistsAllowedVisitType() throws Exception {
        long firstId = createAndAssert("FIRST", "render-first@example.test");
        createAndAssert("REPEAT", "render-repeat@example.test");

        updateStatus(firstId, "CONFIRMED").andExpect(jsonPath("$.status").value("CONFIRMED"));
        updateStatus(firstId, "READY").andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void legacyJapaneseVisitTypeCanAdvanceThroughStaffStatuses() throws Exception {
        jdbcTemplate.update("""
            INSERT INTO reservations (reservation_number, reservation_date, reservation_time,
              participants, customer_name, email, age_group, visit_type, unit_price, total_price, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, "KS-LEGACY-VISIT-TYPE", LocalDate.of(2020, 1, 4), LocalTime.of(11, 0),
            1, "Legacy Render Test", "legacy-render@example.test", "20代", "初めて", 5500, 5500, "RECEIVED");
        Long id = jdbcTemplate.queryForObject(
            "SELECT id FROM reservations WHERE reservation_number = ?", Long.class, "KS-LEGACY-VISIT-TYPE");

        updateStatus(id, "CONFIRMED").andExpect(jsonPath("$.status").value("CONFIRMED"));
        updateStatus(id, "READY").andExpect(jsonPath("$.status").value("READY"));
        updateStatus(id, "COMPLETED").andExpect(jsonPath("$.status").value("COMPLETED"));
        assertEquals("初めて", reservationMapper.selectReservationById(id).getVisitType());
    }

    private long createAndAssert(String visitType, String email) throws Exception {
        String body = """
            {"reservationDate":"2030-01-06","reservationTime":"11:00", "participants":2,
             "customerName":"Render Test","email":"%s","ageGroup":"20代",
             "visitType":"%s","source":"Web","note":"Render profile test"}
            """.formatted(email, visitType);

        String response = mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.status").value("RECEIVED"))
            .andReturn().getResponse().getContentAsString();

        long id = Long.parseLong(response.replaceFirst(".*\\\"id\\\":(\\d+).*", "$1"));
        Reservation saved = reservationMapper.selectReservationById(id);
        assertNotNull(saved);
        assertEquals(visitType, saved.getVisitType());
        assertEquals(email, saved.getEmail());
        return id;
    }

    private org.springframework.test.web.servlet.ResultActions updateStatus(long id, String status) throws Exception {
        return mockMvc.perform(patch("/api/staff/reservations/{id}/status", id)
            .with(user("render-test-staff").roles("STAFF")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status + "\"}"));
    }
}
