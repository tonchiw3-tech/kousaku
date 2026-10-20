package com.example.kousaku.controller;
import com.example.kousaku.dto.InquiryRequest;
import com.example.kousaku.dto.InquiryStatusResponse;
import com.example.kousaku.model.Inquiry;
import com.example.kousaku.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {
 private final InquiryService service;
 public InquiryController(InquiryService s){service=s;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Inquiry create(@Valid @RequestBody InquiryRequest r){return service.create(r);}
 @GetMapping("/status") public ResponseEntity<InquiryStatusResponse> status(@RequestParam String inquiryNumber){Inquiry i=service.findByInquiryNumber(inquiryNumber);return i==null?ResponseEntity.notFound().build():ResponseEntity.ok(InquiryStatusResponse.from(i));}
}