package com.example.kousaku;

import com.example.kousaku.mapper.ReservationMapper;
import com.example.kousaku.model.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TC-041 through TC-045: API checks against the dedicated test schema only. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RemainingTestCasesApiIntegrationTest {
    private static final String STAFF = "remaining-test-staff";

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired ReservationMapper reservationMapper;

    @DynamicPropertySource
    static void staffCredentials(DynamicPropertyRegistry registry) {
        registry.add("STAFF_USERNAME", () -> STAFF);
        registry.add("STAFF_PASSWORD", () -> "remaining-test-password");
    }

    @BeforeEach
    void verifyDedicatedTestDatabase() {
        assertEquals("kousaku_test", jdbcTemplate.queryForObject("SELECT DATABASE()", String.class),
            "統合テストは kousaku_test にのみ接続しなければならない");
    }

    @Test
    void tc041_completedReservationCanBeCorrectedAndAuditIsSaved() throws Exception {
        Reservation reservation = createReservation("tc041@example.test", LocalDate.of(2030, 1, 6), 1);
        reservationMapper.updateStatus(reservation.getId(), "COMPLETED");

        mockMvc.perform(patch("/api/staff/reservations/{id}/correction", reservation.getId())
                .with(user(STAFF).roles("STAFF")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"TC-041 correction\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("READY"));

        assertEquals("READY", reservationMapper.selectReservationById(reservation.getId()).getStatus());
        assertEquals(1, jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM reservation_status_corrections
             WHERE reservation_id=? AND before_status='COMPLETED' AND after_status='READY'
               AND operator_username=? AND reason=?
            """, Integer.class, reservation.getId(), STAFF, "TC-041 correction"));
    }

    @Test
    void tc042_monthlyCalendarSummarizesOnlyActiveReservations() throws Exception {
        Reservation ready = createReservation("tc042-ready@example.test", LocalDate.of(2030, 1, 6), 2);
        Reservation canceled = createReservation("tc042-canceled@example.test", LocalDate.of(2030, 1, 6), 4);
        reservationMapper.updateStatus(ready.getId(), "READY");
        reservationMapper.updateStatus(canceled.getId(), "CANCELED");

        mockMvc.perform(get("/api/staff/calendar").param("year", "2030").param("month", "1")
                .with(user(STAFF).roles("STAFF")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.year").value(2030)).andExpect(jsonPath("$.month").value(1))
            .andExpect(jsonPath("$.days[5].date").value("2030-01-06"))
            .andExpect(jsonPath("$.days[5].reservationCount").value(1))
            .andExpect(jsonPath("$.days[5].participants").value(2))
            .andExpect(jsonPath("$.days[5].preparationIncompleteCount").value(0));
    }

    @Test
    void tc043_dailyCalendarReturnsReservationsAndPreparationCounts() throws Exception {
        Reservation confirmed = createReservation("tc043-confirmed@example.test", LocalDate.of(2030, 1, 13), 3);
        Reservation completed = createReservation("tc043-completed@example.test", LocalDate.of(2030, 1, 13), 2);
        reservationMapper.updateStatus(confirmed.getId(), "CONFIRMED");
        reservationMapper.updateStatus(completed.getId(), "COMPLETED");

        mockMvc.perform(get("/api/staff/calendar/{date}", "2030-01-13").with(user(STAFF).roles("STAFF")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.date").value("2030-01-13"))
            .andExpect(jsonPath("$.reservationCount").value(2)).andExpect(jsonPath("$.participants").value(5))
            .andExpect(jsonPath("$.preparationCompleteCount").value(1))
            .andExpect(jsonPath("$.preparationIncompleteCount").value(1))
            .andExpect(jsonPath("$.reservations.length()").value(2));
    }

    @Test
    void tc044_publicInquiryCanBeCreatedAndLookedUpByNumber() throws Exception {
        String body = "{\"customerName\":\"TC044 Customer\",\"email\":\"tc044@example.test\",\"category\":\"PRICE\",\"message\":\"TC-044 inquiry\"}";
        String response = mockMvc.perform(post("/api/inquiries").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.inquiryNumber").isString())
            .andExpect(jsonPath("$.status").value("NEW")).andReturn().getResponse().getContentAsString();
        String number = jsonField(response, "inquiryNumber");

        mockMvc.perform(get("/api/inquiries/status").param("inquiryNumber", number))
            .andExpect(status().isOk()).andExpect(jsonPath("$.inquiryNumber").value(number))
            .andExpect(jsonPath("$.message").value("TC-044 inquiry")).andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void tc045_staffCanManageInquiryStatusAndReply() throws Exception {
        long inquiryId = createInquiryAndGetId("tc045@example.test");

        mockMvc.perform(get("/api/staff/inquiries").with(user(STAFF).roles("STAFF")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + inquiryId + ")].email").value("tc045@example.test"));
        mockMvc.perform(patch("/api/staff/inquiries/{id}/status", inquiryId).with(user(STAFF).roles("STAFF")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mockMvc.perform(patch("/api/staff/inquiries/{id}/reply", inquiryId).with(user(STAFF).roles("STAFF")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"replyMessage\":\"TC-045 reply\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REPLIED"))
            .andExpect(jsonPath("$.replyMessage").value("TC-045 reply"));
        mockMvc.perform(get("/api/staff/inquiries/{id}", inquiryId).with(user(STAFF).roles("STAFF")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REPLIED"));
    }

    private Reservation createReservation(String email, LocalDate date, int participants) throws Exception {
        String body = "{\"reservationDate\":\"" + date + "\",\"reservationTime\":\"11:00\",\"participants\":" + participants
            + ",\"customerName\":\"API Test\",\"email\":\"" + email + "\",\"ageGroup\":\"20代\",\"visitType\":\"FIRST\"}";
        String response = mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return reservationMapper.selectReservationById(Long.parseLong(jsonField(response, "id")));
    }

    private long createInquiryAndGetId(String email) throws Exception {
        String body = "{\"customerName\":\"TC045 Staff\",\"email\":\"" + email + "\",\"category\":\"OTHER\",\"message\":\"TC-045 inquiry\"}";
        String response = mockMvc.perform(post("/api/inquiries").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return Long.parseLong(jsonField(response, "id"));
    }

    private String jsonField(String json, String name) {
        Matcher matcher = Pattern.compile("\\\"" + name + "\\\":(?:\\\")?([^,\\\"}]+)").matcher(json);
        assertTrue(matcher.find(), name + " must be in response: " + json);
        return matcher.group(1);
    }
}
