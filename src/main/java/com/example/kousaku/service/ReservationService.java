package com.example.kousaku.service;

import com.example.kousaku.dto.ReservationRequest;
import com.example.kousaku.dto.ReservationResponse;
import com.example.kousaku.mapper.ReservationMapper;
import com.example.kousaku.model.Reservation;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

@Service
public class ReservationService {
    private static final BigDecimal UNIT_PRICE = BigDecimal.valueOf(5500);
    private final ReservationMapper mapper;

    public ReservationService(ReservationMapper mapper) { this.mapper = mapper; }

    public List<Reservation> findAll() {
        return mapper.selectReservations();
    }

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
