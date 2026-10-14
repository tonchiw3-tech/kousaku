package com.example.kousaku.mapper;

import com.example.kousaku.model.Reservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ReservationMapper {
    @Insert("""
        INSERT INTO reservations (reservation_number, reservation_date, reservation_time,
          participants, customer_name, email, age_group, visit_type, unit_price, total_price, status)
        VALUES (#{reservationNumber}, #{reservationDate}, #{reservationTime}, #{participants},
          #{customerName}, #{email}, #{ageGroup}, #{visitType}, #{unitPrice}, #{totalPrice}, #{status})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertReservation(Reservation reservation);
}
