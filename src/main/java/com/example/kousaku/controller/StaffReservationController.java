package com.example.kousaku.controller;
import com.example.kousaku.model.Reservation;
import com.example.kousaku.service.ReservationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController
@RequestMapping("/api/staff/reservations")
public class StaffReservationController {
    private final ReservationService service;
    public StaffReservationController(ReservationService service) { this.service = service; }
    @GetMapping public List<Reservation> list() { return service.findAll(); }
    @GetMapping("/{id}") public Reservation detail(@PathVariable Long id) { Reservation r = service.findById(id); if (r == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); return r; }
    @PatchMapping("/{id}/status")
    public Reservation updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        try { Reservation r = service.updateStatus(id, request == null ? null : request.status()); if (r == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); return r; }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is invalid", e); }
    }
    @PatchMapping("/{id}/cancel")
    public Reservation cancel(@PathVariable Long id) {
        Reservation r = service.cancel(id);
        if (r == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return r;
    }

    @PatchMapping("/{id}/revert-status")
    public Reservation revertStatus(@PathVariable Long id) {
        try {
            Reservation r = service.revertStatus(id);
            if (r == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            return r;
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "status cannot be reverted", e);
        }
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.noContent().build();
    }
    public record StatusRequest(String status) {}
}
