package com.example.kousaku.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequest(
    @NotNull LocalDate reservationDate,
    @NotNull LocalTime reservationTime,
    @NotNull @Min(1) @Max(10) Integer participants,
    @NotBlank String customerName,
    @NotBlank
    @Email(message = "メールアドレスを正しい形式で入力してください。")
    @Pattern(
        regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
        message = "メールアドレスを正しい形式で入力してください。"
    )
    String email,
    @NotBlank @Pattern(regexp = "10代|20代|30代|40代|50代|60代以上") String ageGroup,
    @NotBlank @Pattern(regexp = "FIRST|REPEAT") String visitType,
    @Size(max = 100) String source,
    @Size(max = 1000) String note
) {
    private static final java.util.Set<LocalTime> AVAILABLE_TIMES =
        java.util.Set.of(LocalTime.of(11, 0), LocalTime.of(13, 0), LocalTime.of(15, 0));

    @AssertTrue(message = "reservationTime must be 11:00, 13:00, or 15:00")
    public boolean isReservationTimeAvailable() {
        return reservationTime == null || AVAILABLE_TIMES.contains(reservationTime);
    }

    @AssertTrue(message = "reservationDate must be Saturday or Sunday")
    public boolean isReservationDateOnWeekend() {
        return reservationDate == null
            || reservationDate.getDayOfWeek() == DayOfWeek.SATURDAY
            || reservationDate.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
