package com.example.kousaku.mapper;

import com.example.kousaku.model.Reservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface ReservationMapper {
    @Select("""
        SELECT id, reservation_number AS reservationNumber, reservation_date AS reservationDate,
          reservation_time AS reservationTime, participants, customer_name AS customerName,
          email, age_group AS ageGroup, visit_type AS visitType, source, note, unit_price AS unitPrice,
          total_price AS totalPrice, status, created_at AS createdAt, updated_at AS updatedAt
        FROM reservations
        ORDER BY reservation_date ASC, reservation_time ASC, id ASC
        """)
    List<Reservation> selectReservations();

    @Select("SELECT id, reservation_number AS reservationNumber, reservation_date AS reservationDate, reservation_time AS reservationTime, participants, customer_name AS customerName, email, age_group AS ageGroup, visit_type AS visitType, source, note, unit_price AS unitPrice, total_price AS totalPrice, status, created_at AS createdAt, updated_at AS updatedAt FROM reservations WHERE id = #{id}")
    Reservation selectReservationById(Long id);

    @Update("UPDATE reservations SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@org.apache.ibatis.annotations.Param("id") Long id,
                     @org.apache.ibatis.annotations.Param("status") String status);

    @Update("UPDATE reservations SET status = 'READY', updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 'COMPLETED'")
    int correctCompletedToReady(Long id);

    @Delete("DELETE FROM reservations WHERE id = #{id}")
    int deleteById(@org.apache.ibatis.annotations.Param("id") Long id);

    @Insert("""
        INSERT INTO reservations (reservation_number, reservation_date, reservation_time,
          participants, customer_name, email, age_group, visit_type, source, note, unit_price, total_price, status)
        VALUES (#{reservationNumber}, #{reservationDate}, #{reservationTime}, #{participants},
          #{customerName}, #{email}, #{ageGroup}, #{visitType}, #{source}, #{note}, #{unitPrice}, #{totalPrice}, #{status})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertReservation(Reservation reservation);
}
