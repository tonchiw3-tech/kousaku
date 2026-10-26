package com.example.kousaku;

import com.example.kousaku.mapper.ReservationMapper;
import com.example.kousaku.model.Reservation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("render")
class RenderReservationApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ReservationMapper reservationMapper;

    @Test
    void createWithRenderProfileReturns201AndPersistsAllowedVisitType() throws Exception {
        createAndAssert("FIRST", "render-first@example.test");
        createAndAssert("REPEAT", "render-repeat@example.test");
    }

    private void createAndAssert(String visitType, String email) throws Exception {
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
    }
}
