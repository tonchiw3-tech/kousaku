package com.example.kousaku.service;

import com.example.kousaku.dto.ReservationRequest;
import com.example.kousaku.dto.ReservationResponse;
import com.example.kousaku.mapper.ReservationMapper;
import com.example.kousaku.model.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

@Service
public class ReservationService {
    public static final java.util.Set<String> VALID_STATUSES = java.util.Set.of("RECEIVED", "CONFIRMED", "READY", "COMPLETED", "CANCELED");
    private static final BigDecimal UNIT_PRICE = BigDecimal.valueOf(5500);
    private final ReservationMapper mapper;
    private final Clock clock;

    @Autowired
    public ReservationService(ReservationMapper mapper) { this(mapper, Clock.system(ZoneId.of("Asia/Tokyo"))); }

    public ReservationService(ReservationMapper mapper, Clock clock) {
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<Reservation> findAll() {
        return mapper.selectReservations();
    }

    public Reservation findById(Long id) { return mapper.selectReservationById(id); }

    public Reservation updateStatus(Long id, String status) {
        if (status == null || !VALID_STATUSES.contains(status)) throw new IllegalArgumentException("status is invalid");
        Reservation current = mapper.selectReservationById(id);
        if (current == null) return null;
        if ("CANCELED".equals(current.getStatus()) || "COMPLETED".equals(current.getStatus())) {
            throw new IllegalStateException("status cannot be changed");
        }
        String expected = switch (current.getStatus()) {
            case "RECEIVED" -> "CONFIRMED";
            case "CONFIRMED" -> "READY";
            case "READY" -> "COMPLETED";
            default -> throw new IllegalStateException("status transition is invalid");
        };
        if (!expected.equals(status)) throw new IllegalStateException("status transition is invalid");
        if ("COMPLETED".equals(status) && isBeforeReservationStart(current)) {
            throw new IllegalStateException("reservation has not started");
        }
        if (mapper.updateStatus(id, status) == 0) return null;
        return mapper.selectReservationById(id);
    }

    private boolean isBeforeReservationStart(Reservation reservation) {
        LocalDateTime start = LocalDateTime.of(reservation.getReservationDate(), reservation.getReservationTime());
        return start.isAfter(LocalDateTime.now(clock));
    }

    public Reservation cancel(Long id) {
        Reservation current = mapper.selectReservationById(id);
        if (current == null) return null;
        if ("CANCELED".equals(current.getStatus()) || "COMPLETED".equals(current.getStatus())) {
            throw new IllegalStateException("status cannot be canceled");
        }
        if (mapper.updateStatus(id, "CANCELED") == 0) return null;
        return mapper.selectReservationById(id);
    }

    public Reservation revertStatus(Long id) {
        Reservation current = mapper.selectReservationById(id);
        if (current == null) return null;
        String previous = switch (current.getStatus()) {
            case "COMPLETED" -> "READY";
            case "READY" -> "CONFIRMED";
            case "CONFIRMED" -> "RECEIVED";
            case "RECEIVED", "CANCELED" -> throw new IllegalStateException("status cannot be reverted");
            default -> throw new IllegalStateException("status cannot be reverted");
        };
        if (mapper.updateStatus(id, previous) == 0) return null;
        return mapper.selectReservationById(id);
    }

    public boolean delete(Long id) { return mapper.deleteById(id) > 0; }

    public ReservationResponse create(ReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setReservationNumber(generateNumber());
        reservation.setReservationDate(request.reservationDate());
        reservation.setReservationTime(request.reservationTime());
        reservation.setParticipants(request.participants());
        reservation.setCustomerName(request.customerName().trim());
        reservation.setEmail(request.email().trim());
        reservation.setAgeGroup(request.ageGroup());
        reservation.setVisitType(normalizeVisitType(request.visitType()));
        reservation.setUnitPrice(UNIT_PRICE);
        reservation.setTotalPrice(UNIT_PRICE.multiply(BigDecimal.valueOf(request.participants())));
        reservation.setStatus("RECEIVED");
        mapper.insertReservation(reservation);
        return new ReservationResponse(reservation.getId(), reservation.getReservationNumber(),
            reservation.getParticipants(), reservation.getTotalPrice(), reservation.getStatus(), "予約データを登録しました");
    }

    private String generateNumber() {
        return "KS-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String normalizeVisitType(String value) {
        return switch (value) {
            case "FIRST", "初めて" -> "FIRST";
            case "REPEAT", "2回目", "3回目以上" -> "REPEAT";
            default -> throw new IllegalArgumentException("visitType is invalid");
        };
    }
}
