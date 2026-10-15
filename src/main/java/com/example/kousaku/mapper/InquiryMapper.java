package com.example.kousaku.mapper;
import com.example.kousaku.model.Inquiry;
import org.apache.ibatis.annotations.*;
import java.util.List;
@Mapper public interface InquiryMapper {
 @Select("SELECT id,inquiry_number AS inquiryNumber,customer_name AS customerName,email,category,message,status,reply_message AS replyMessage,replied_at AS repliedAt,created_at AS createdAt,updated_at AS updatedAt FROM inquiries ORDER BY created_at DESC,id DESC") List<Inquiry> findAll();
 @Select("SELECT id,inquiry_number AS inquiryNumber,customer_name AS customerName,email,category,message,status,reply_message AS replyMessage,replied_at AS repliedAt,created_at AS createdAt,updated_at AS updatedAt FROM inquiries WHERE id=#{id}") Inquiry findById(Long id);
 @Select("SELECT id,inquiry_number AS inquiryNumber,customer_name AS customerName,email,category,message,status,reply_message AS replyMessage,replied_at AS repliedAt,created_at AS createdAt,updated_at AS updatedAt FROM inquiries WHERE inquiry_number=#{inquiryNumber}") Inquiry findByInquiryNumber(String inquiryNumber);
 @Select("SELECT id,inquiry_number AS inquiryNumber,customer_name AS customerName,email,category,message,status,reply_message AS replyMessage,replied_at AS repliedAt,created_at AS createdAt,updated_at AS updatedAt FROM inquiries WHERE inquiry_number=#{inquiryNumber}") Inquiry findByInquiryNumber(String inquiryNumber);
 @Select("SELECT id,inquiry_number AS inquiryNumber,customer_name AS customerName,email,category,message,status,reply_message AS replyMessage,replied_at AS repliedAt,created_at AS createdAt,updated_at AS updatedAt FROM inquiries WHERE inquiry_number=#{inquiryNumber}") Inquiry findByInquiryNumber(String inquiryNumber);
 @Insert("INSERT INTO inquiries(inquiry_number,customer_name,email,category,message,status) VALUES(#{inquiryNumber},#{customerName},#{email},#{category},#{message},'NEW')") @Options(useGeneratedKeys=true,keyProperty="id") int insert(Inquiry inquiry);
 @Update("UPDATE inquiries SET status=#{status},updated_at=CURRENT_TIMESTAMP WHERE id=#{id}") int updateStatus(@Param("id") Long id,@Param("status") String status);
 @Update("UPDATE inquiries SET reply_message=#{replyMessage},status='REPLIED',replied_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE id=#{id}") int updateReply(@Param("id") Long id,@Param("replyMessage") String replyMessage);
}
