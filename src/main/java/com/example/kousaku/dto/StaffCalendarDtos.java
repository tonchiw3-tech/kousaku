package com.example.kousaku.dto;

import com.example.kousaku.model.Reservation;
import java.time.LocalDate;
import java.util.List;

public final class StaffCalendarDtos {
    private StaffCalendarDtos() {}
    public record Day(LocalDate date, int reservationCount, int participants, int preparationIncompleteCount) {}
    public record Month(int year, int month, List<Day> days) {}
    public record DayDetail(LocalDate date, int reservationCount, int participants, int preparationCompleteCount,
                            int preparationIncompleteCount, List<Reservation> reservations) {}
}
