package com.example.kousaku.controller;
import com.example.kousaku.model.Reservation;
import com.example.kousaku.service.ReservationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/staff/reservations")
public class StaffReservationController {
    private final ReservationService service;
    public StaffReservationController(ReservationService service) { this.service = service; }
    @GetMapping public List<Reservation> list() { return service.findAll(); }
}
