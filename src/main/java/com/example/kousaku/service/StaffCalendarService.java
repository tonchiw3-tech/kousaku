package com.example.kousaku.service;

import com.example.kousaku.dto.StaffCalendarDtos;
import com.example.kousaku.model.Reservation;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class StaffCalendarService {
    private final ReservationService reservations;
    public StaffCalendarService(ReservationService reservations) { this.reservations = reservations; }

    public StaffCalendarDtos.Month month(int year, int month) {
        LocalDate first = LocalDate.of(year, month, 1);
        List<StaffCalendarDtos.Day> days = IntStream.range(0, first.lengthOfMonth())
            .mapToObj(first::plusDays).map(this::summary).toList();
        return new StaffCalendarDtos.Month(year, month, days);
    }

    public StaffCalendarDtos.DayDetail day(LocalDate date) {
        List<Reservation> items = active(date).stream()
            .sorted(Comparator.comparing(Reservation::getReservationTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
        int people = items.stream().mapToInt(r -> r.getParticipants() == null ? 0 : r.getParticipants()).sum();
        int complete = (int) items.stream().filter(this::preparationComplete).count();
        return new StaffCalendarDtos.DayDetail(date, items.size(), people, complete, items.size() - complete, items);
    }

    private StaffCalendarDtos.Day summary(LocalDate date) {
        List<Reservation> items = active(date);
        int people = items.stream().mapToInt(r -> r.getParticipants() == null ? 0 : r.getParticipants()).sum();
        int incomplete = (int) items.stream().filter(r -> !preparationComplete(r)).count();
        return new StaffCalendarDtos.Day(date, items.size(), people, incomplete);
    }
    private List<Reservation> active(LocalDate date) {
        return reservations.findAll().stream().filter(r -> date.equals(r.getReservationDate()))
            .filter(r -> !"CANCELED".equals(r.getStatus())).toList();
    }
    private boolean preparationComplete(Reservation r) {
        return "READY".equals(r.getStatus()) || "COMPLETED".equals(r.getStatus());
    }
}
