package com.example.kousaku.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequest(
    @NotNull LocalDate reservationDate,
    @NotNull LocalTime reservationTime,
    @NotNull @Min(1) Integer participants,
    @NotBlank String customerName,
    @NotBlank @Email String email,
    @NotBlank String ageGroup,
    @NotBlank String visitType
) {}
