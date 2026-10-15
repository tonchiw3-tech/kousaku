package com.example.kousaku.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/staff/reservations")
	public String staffReservations() {
		return "staff";
	}

	@GetMapping("/staff/reservations/{id}")
	public String staffReservationDetail() { return "staff-reservation-detail"; }

	@GetMapping("/staff/calendar")
	public String staffCalendar() { return "staff-calendar"; }

	@GetMapping("/staff/calendar/{date}")
	public String staffCalendarDay() { return "staff-calendar-day"; }

	@GetMapping("/inquiries") public String inquiries() { return "inquiry"; }
	@GetMapping("/inquiries/status") public String inquiryStatus() { return "inquiry-status"; }
	@GetMapping("/staff/inquiries") public String staffInquiries() { return "staff-inquiries"; }
	@GetMapping("/staff/inquiries/{id}") public String staffInquiryDetail() { return "staff-inquiry-detail"; }
}
