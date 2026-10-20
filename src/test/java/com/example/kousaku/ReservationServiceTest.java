package com.example.kousaku;

import com.example.kousaku.mapper.ReservationMapper;
import com.example.kousaku.model.Reservation;
import com.example.kousaku.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReservationServiceTest {
    @Test void futureReservationCannotBeCompleted() {
        ReservationMapper mapper = Mockito.mock(ReservationMapper.class);
        Reservation r = reservation("2030-01-01", "10:00"); r.setStatus("READY");
        Mockito.when(mapper.selectReservationById(1L)).thenReturn(r);
        ReservationService service = new ReservationService(mapper, clock("2030-01-01T09:00:00+09:00"));
        assertThrows(IllegalStateException.class, () -> service.updateStatus(1L, "COMPLETED"));
        Mockito.verify(mapper, Mockito.never()).updateStatus(1L, "COMPLETED");
    }

    @Test void startedReservationCanBeCompletedByStaff() {
        ReservationMapper mapper = Mockito.mock(ReservationMapper.class);
        Reservation r = reservation("2030-01-01", "10:00"); r.setStatus("READY");
        Mockito.when(mapper.selectReservationById(1L)).thenReturn(r);
        Mockito.when(mapper.updateStatus(1L, "COMPLETED")).thenReturn(1);
        ReservationService service = new ReservationService(mapper, clock("2030-01-01T10:00:01+09:00"));
        assertDoesNotThrow(() -> service.updateStatus(1L, "COMPLETED"));
        Mockito.verify(mapper).updateStatus(1L, "COMPLETED");
    }

    private static Reservation reservation(String date, String time) {
        Reservation r = new Reservation(); r.setId(1L);
        r.setReservationDate(java.time.LocalDate.parse(date)); r.setReservationTime(java.time.LocalTime.parse(time)); return r;
    }
    private static Clock clock(String value) { return Clock.fixed(Instant.parse(value), ZoneId.of("Asia/Tokyo")); }
}
