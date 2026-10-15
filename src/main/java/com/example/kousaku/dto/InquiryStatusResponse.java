package com.example.kousaku.dto;
import com.example.kousaku.model.Inquiry;
import java.time.LocalDateTime;
public record InquiryStatusResponse(String inquiryNumber, LocalDateTime createdAt, String category, String message, String status, String replyMessage, LocalDateTime repliedAt) {
 public static InquiryStatusResponse from(Inquiry i){return new InquiryStatusResponse(i.getInquiryNumber(),i.getCreatedAt(),i.getCategory(),i.getMessage(),i.getStatus(),i.getReplyMessage(),i.getRepliedAt());}
}
