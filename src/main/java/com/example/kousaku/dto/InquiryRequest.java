package com.example.kousaku.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record InquiryRequest(@NotBlank @Size(max=100) String customerName, @NotBlank @Email @Size(max=254) String email,
    @NotBlank String category, @NotBlank @Size(max=10000) String message) {}
