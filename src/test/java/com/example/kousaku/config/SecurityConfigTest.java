package com.example.kousaku.config;

import com.example.kousaku.controller.StaffReservationController;
import com.example.kousaku.model.Reservation;
import com.example.kousaku.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {
    @Test
    void staffHasOnlyTheUnifiedStaffAuthority() {
        UserDetailsService users = new SecurityConfig().users("staff-user", "staff-password");
        UserDetails staff = users.loadUserByUsername("staff-user");

        assertTrue(staff.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")));
    }

    @Test
    void staffCanUseCorrectionEndpoint() {
        ReservationService service = Mockito.mock(ReservationService.class);
        Reservation expected = new Reservation();
        Mockito.when(service.correctCompletedToReady(5L, "staff-user", "reason")).thenReturn(expected);
        StaffReservationController controller = new StaffReservationController(service);
        var staff = new UsernamePasswordAuthenticationToken("staff-user", "N/A", List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
        assertSame(expected,
            controller.correctCompletedToReady(5L, new StaffReservationController.CorrectionRequest("reason"), staff));
    }
}
