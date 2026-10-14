package com.example.kousaku.controller;

import com.example.kousaku.dto.ReservationRequest;
import com.example.kousaku.dto.ReservationResponse;
import com.example.kousaku.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService service;
    public ReservationController(ReservationService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
        return service.create(request);
    }
}
