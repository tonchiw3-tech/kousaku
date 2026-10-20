package com.example.kousaku;

import com.example.kousaku.mapper.ReservationMapper;
import com.example.kousaku.mapper.ReservationCorrectionMapper;
import com.example.kousaku.model.Reservation;
import com.example.kousaku.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test void progressChecksCanBeCleared() {
        ReservationMapper mapper = Mockito.mock(ReservationMapper.class);
        Reservation r = reservation("2030-01-01", "10:00"); r.setStatus("READY");
        Mockito.when(mapper.selectReservationById(1L)).thenReturn(r);
        Mockito.when(mapper.updateStatus(1L, "CONFIRMED")).thenReturn(1);
        Mockito.when(mapper.updateStatus(1L, "RECEIVED")).thenReturn(1);
        ReservationService service = new ReservationService(mapper, clock("2030-01-01T12:00:00+09:00"));

        assertDoesNotThrow(() -> service.updateStatus(1L, "CONFIRMED"));
        assertDoesNotThrow(() -> service.updateStatus(1L, "RECEIVED"));
        Mockito.verify(mapper).updateStatus(1L, "CONFIRMED");
        Mockito.verify(mapper).updateStatus(1L, "RECEIVED");
    }

    @Test void preparationCannotBeEnabledWithoutConfirmation() {
        ReservationMapper mapper = Mockito.mock(ReservationMapper.class);
        Reservation r = reservation("2030-01-01", "10:00"); r.setStatus("RECEIVED");
        Mockito.when(mapper.selectReservationById(1L)).thenReturn(r);
        ReservationService service = new ReservationService(mapper, clock("2030-01-01T12:00:00+09:00"));

        assertThrows(IllegalStateException.class, () -> service.updateStatus(1L, "READY"));
        Mockito.verify(mapper, Mockito.never()).updateStatus(1L, "READY");
    }

    @Test void canceledReservationCannotHaveProgressChanged() {
        ReservationMapper mapper = Mockito.mock(ReservationMapper.class);
        Reservation r = reservation("2030-01-01", "10:00"); r.setStatus("CANCELED");
        Mockito.when(mapper.selectReservationById(1L)).thenReturn(r);
        ReservationService service = new ReservationService(mapper, clock("2030-01-01T12:00:00+09:00"));

        assertThrows(IllegalStateException.class, () -> service.updateStatus(1L, "CONFIRMED"));
        Mockito.verify(mapper, Mockito.never()).updateStatus(Mockito.anyLong(), Mockito.anyString());
    }

    @Test void staffCorrectionRequiresReasonAndWritesAuditRecord() {
        ReservationMapper mapper = Mockito.mock(ReservationMapper.class);
        ReservationCorrectionMapper corrections = Mockito.mock(ReservationCorrectionMapper.class);
        Reservation r = reservation("2030-01-01", "10:00"); r.setStatus("COMPLETED");
        Mockito.when(mapper.selectReservationById(1L)).thenReturn(r);
        Mockito.when(mapper.correctCompletedToReady(1L)).thenReturn(1);
        ReservationService service = new ReservationService(mapper, corrections, clock("2030-01-01T12:00:00+09:00"));

        assertThrows(IllegalArgumentException.class, () -> service.correctCompletedToReady(1L, "admin", "  "));
        Reservation corrected = service.correctCompletedToReady(1L, "admin", "入力ミスのため");
        assertEquals(r, corrected);
        Mockito.verify(corrections).insert(Mockito.argThat(x -> x.reservationId().equals(1L)
            && x.beforeStatus().equals("COMPLETED") && x.afterStatus().equals("READY")
            && x.operatorUsername().equals("admin") && x.reason().equals("入力ミスのため")));
    }

    private static Reservation reservation(String date, String time) {
        Reservation r = new Reservation(); r.setId(1L);
        r.setReservationDate(java.time.LocalDate.parse(date)); r.setReservationTime(java.time.LocalTime.parse(time)); return r;
    }
    private static Clock clock(String value) { return Clock.fixed(Instant.parse(value), ZoneId.of("Asia/Tokyo")); }
}
