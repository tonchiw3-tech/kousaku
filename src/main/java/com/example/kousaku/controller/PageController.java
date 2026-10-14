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
}
