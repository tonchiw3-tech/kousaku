package com.example.kousaku.dto;

import java.math.BigDecimal;

public record ReservationResponse(Long id, String reservationNumber, Integer participants,
                                  BigDecimal totalPrice, String status, String message) {}
