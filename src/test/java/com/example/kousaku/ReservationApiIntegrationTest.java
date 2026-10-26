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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationApiIntegrationTest {
    private static final String STAFF_USERNAME = "reservation-api-test-staff";
    private static final String STAFF_PASSWORD = "reservation-api-test-password";

    @Autowired MockMvc mockMvc;
    @Autowired ReservationMapper reservationMapper;
    @Autowired JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void staffCredentials(DynamicPropertyRegistry registry) {
        registry.add("STAFF_USERNAME", () -> STAFF_USERNAME);
        registry.add("STAFF_PASSWORD", () -> STAFF_PASSWORD);
    }

    @BeforeEach
    void verifyTestDatabase() {
        assertEquals("kousaku_test", jdbcTemplate.queryForObject("SELECT DATABASE()", String.class),
            "予約API統合テストは kousaku_test にのみ接続すること");
    }

    @Test
    void createReturns201AndPersistsReservationWithReservationNumber() throws Exception {
        Reservation saved = createReservation("create@example.test", 3);

        assertNotNull(saved.getId());
        assertTrue(saved.getReservationNumber().matches("KS-\\d{14}-[A-F0-9]{6}"));
        assertEquals("RECEIVED", saved.getStatus());
        assertEquals("Create Test", saved.getCustomerName());
        assertEquals(3, saved.getParticipants());
        assertEquals(new BigDecimal("16500"), saved.getTotalPrice());
        assertEquals("FIRST", saved.getVisitType());
        assertEquals("Instagram", saved.getSource());
        assertEquals("テスト用の備考", saved.getNote());
    }

    @Test
    void staffCanListAndRetrieveReservationDetail() throws Exception {
        Reservation saved = createReservation("detail@example.test", 2);

        mockMvc.perform(get("/api/staff/reservations").with(user(STAFF_USERNAME).roles("STAFF")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + saved.getId() + ")].email").value("detail@example.test"));
        mockMvc.perform(get("/api/staff/reservations/{id}", saved.getId()).with(user(STAFF_USERNAME).roles("STAFF")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservationNumber").value(saved.getReservationNumber()))
            .andExpect(jsonPath("$.source").value("Instagram"))
            .andExpect(jsonPath("$.note").value("テスト用の備考"))
            .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void staffCanUpdateStatusThroughAllowedTransitions() throws Exception {
        Reservation saved = createReservation("status@example.test", 1);

        updateStatus(saved.getId(), "CONFIRMED").andExpect(jsonPath("$.status").value("CONFIRMED"));
        updateStatus(saved.getId(), "READY").andExpect(jsonPath("$.status").value("READY"));
        assertEquals("READY", reservationMapper.selectReservationById(saved.getId()).getStatus());
    }

    @Test
    void staffCanCancelReservation() throws Exception {
        Reservation saved = createReservation("cancel@example.test", 1);

        mockMvc.perform(patch("/api/staff/reservations/{id}/cancel", saved.getId())
                .with(user(STAFF_USERNAME).roles("STAFF")).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELED"));
        assertEquals("CANCELED", reservationMapper.selectReservationById(saved.getId()).getStatus());
    }

    @Test
    void staffCanRevertConfirmedStatusToReceived() throws Exception {
        Reservation saved = createReservationWithStatus("revert-confirmed@example.test", "CONFIRMED");

        revertStatus(saved.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.status").value("RECEIVED"));
        assertEquals("RECEIVED", reservationMapper.selectReservationById(saved.getId()).getStatus());
    }

    @Test
    void staffCanRevertReadyStatusToConfirmed() throws Exception {
        Reservation saved = createReservationWithStatus("revert-ready@example.test", "READY");

        revertStatus(saved.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
        assertEquals("CONFIRMED", reservationMapper.selectReservationById(saved.getId()).getStatus());
    }

    @Test
    void staffCanRevertCompletedStatusToReady() throws Exception {
        Reservation saved = createReservationWithStatus("revert-completed@example.test", "COMPLETED");

        revertStatus(saved.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.status").value("READY"));
        assertEquals("READY", reservationMapper.selectReservationById(saved.getId()).getStatus());
    }

    @Test
    void staffCannotRevertReceivedStatus() throws Exception {
        Reservation saved = createReservation("revert-received@example.test", 1);

        revertStatus(saved.getId()).andExpect(status().isConflict());
        assertEquals("RECEIVED", reservationMapper.selectReservationById(saved.getId()).getStatus());
    }

    @Test
    void staffCannotRevertCanceledStatus() throws Exception {
        Reservation saved = createReservationWithStatus("revert-canceled@example.test", "CANCELED");

        revertStatus(saved.getId()).andExpect(status().isConflict());
        assertEquals("CANCELED", reservationMapper.selectReservationById(saved.getId()).getStatus());
    }

    @Test
    void staffCanDeleteReservation() throws Exception {
        Reservation saved = createReservation("delete@example.test", 1);

        mockMvc.perform(delete("/api/staff/reservations/{id}", saved.getId())
                .with(user(STAFF_USERNAME).roles("STAFF")).with(csrf()))
            .andExpect(status().isNoContent());
        assertEquals(null, reservationMapper.selectReservationById(saved.getId()));
    }

    @Test
    void createRejectsMissingRequiredFieldInvalidParticipantsAndInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content("{\"reservationTime\":\"11:00\",\"participants\":1,\"customerName\":\"A\",\"email\":\"valid@example.test\",\"ageGroup\":\"20代\",\"visitType\":\"FIRST\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("invalid-participants@example.test", 0)))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("not-an-email", 1)))
            .andExpect(status().isBadRequest());
        int countBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Integer.class);
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("invalid@example", 1)))
            .andExpect(status().isBadRequest());
        assertEquals(countBefore, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Integer.class));
    }

    @Test
    void createRejectsValuesOutsideTheReservationRules() throws Exception {
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("too-many@example.test", 11)))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("weekday@example.test", 1).replace("2030-01-06", "2030-01-07")))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("time@example.test", 1).replace("11:00", "10:00")))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("age@example.test", 1).replace("20代", "20s")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedUserIsRedirectedToLoginForStaffApi() throws Exception {
        mockMvc.perform(get("/api/staff/reservations"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void userWithoutStaffRoleIsForbiddenFromStaffApi() throws Exception {
        mockMvc.perform(get("/api/staff/reservations").with(user("customer").roles("CUSTOMER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void staffCannotUseInvalidStatusTransitionOrStatusValue() throws Exception {
        Reservation saved = createReservation("invalid-transition@example.test", 1);

        updateStatus(saved.getId(), "READY").andExpect(status().isConflict());
        updateStatus(saved.getId(), "UNKNOWN").andExpect(status().isBadRequest());
        assertEquals("RECEIVED", reservationMapper.selectReservationById(saved.getId()).getStatus());
    }

    private Reservation createReservation(String email, int participants) throws Exception {
        var result = mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(email, participants)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reservationNumber").isString())
            .andExpect(jsonPath("$.status").value("RECEIVED"))
            .andReturn();
        Matcher id = Pattern.compile("\\\"id\\\":(\\d+)").matcher(result.getResponse().getContentAsString());
        assertTrue(id.find(), "予約登録レスポンスに id が含まれること");
        return reservationMapper.selectReservationById(Long.parseLong(id.group(1)));
    }

    private org.springframework.test.web.servlet.ResultActions updateStatus(long id, String status) throws Exception {
        return mockMvc.perform(patch("/api/staff/reservations/{id}/status", id)
            .with(user(STAFF_USERNAME).roles("STAFF")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status + "\"}"));
    }

    private Reservation createReservationWithStatus(String email, String status) throws Exception {
        Reservation saved = createReservation(email, 1);
        assertEquals(1, reservationMapper.updateStatus(saved.getId(), status));
        return reservationMapper.selectReservationById(saved.getId());
    }

    private org.springframework.test.web.servlet.ResultActions revertStatus(long id) throws Exception {
        return mockMvc.perform(patch("/api/staff/reservations/{id}/revert-status", id)
            .with(user(STAFF_USERNAME).roles("STAFF")).with(csrf()));
    }

    private String requestJson(String email, int participants) {
        return "{\"reservationDate\":\"" + LocalDate.of(2030, 1, 6) + "\",\"reservationTime\":\""
            + LocalTime.of(11, 0) + "\",\"participants\":" + participants + ",\"customerName\":\"Create Test\",\"email\":\""
            + email + "\",\"ageGroup\":\"20代\",\"visitType\":\"FIRST\",\"source\":\"Instagram\",\"note\":\"テスト用の備考\"}";
    }
}
