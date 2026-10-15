package com.example.kousaku.model;

import java.time.LocalDateTime;

public class Inquiry {
    private Long id; private String inquiryNumber; private String customerName; private String email;
    private String category; private String message; private String status; private String replyMessage;
    private LocalDateTime repliedAt; private LocalDateTime createdAt; private LocalDateTime updatedAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getInquiryNumber(){return inquiryNumber;} public void setInquiryNumber(String v){inquiryNumber=v;}
    public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getReplyMessage(){return replyMessage;} public void setReplyMessage(String v){replyMessage=v;}
    public LocalDateTime getRepliedAt(){return repliedAt;} public void setRepliedAt(LocalDateTime v){repliedAt=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
