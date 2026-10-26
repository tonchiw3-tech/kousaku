package com.example.kousaku.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reservation {
    private Long id;
    private String reservationNumber;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer participants;
    private String customerName;
    private String email;
    private String ageGroup;
    private String visitType;
    private String source;
    private String note;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReservationNumber() { return reservationNumber; }
    public void setReservationNumber(String value) { this.reservationNumber = value; }
    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate value) { this.reservationDate = value; }
    public LocalTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalTime value) { this.reservationTime = value; }
    public Integer getParticipants() { return participants; }
    public void setParticipants(Integer value) { this.participants = value; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String value) { this.customerName = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String value) { this.ageGroup = value; }
    public String getVisitType() { return visitType; }
    public void setVisitType(String value) { this.visitType = value; }
    public String getSource() { return source; }
    public void setSource(String value) { this.source = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal value) { this.unitPrice = value; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal value) { this.totalPrice = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
