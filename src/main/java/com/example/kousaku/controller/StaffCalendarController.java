package com.example.kousaku.controller;

import com.example.kousaku.dto.StaffCalendarDtos;
import com.example.kousaku.service.StaffCalendarService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/staff/calendar")
public class StaffCalendarController {
    private final StaffCalendarService service;
    public StaffCalendarController(StaffCalendarService service) { this.service = service; }
    @GetMapping public StaffCalendarDtos.Month month(@RequestParam int year, @RequestParam int month) { return service.month(year, month); }
    @GetMapping("/{date}") public StaffCalendarDtos.DayDetail day(@PathVariable LocalDate date) { return service.day(date); }
}
