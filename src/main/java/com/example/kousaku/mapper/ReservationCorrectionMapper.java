package com.example.kousaku.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationCorrectionMapper {
    @Insert("""
        INSERT INTO reservation_status_corrections
          (reservation_id, before_status, after_status, changed_at, operator_username, reason)
        VALUES (#{reservationId}, #{beforeStatus}, #{afterStatus}, #{changedAt}, #{operatorUsername}, #{reason})
        """)
    int insert(CorrectionRecord record);

    record CorrectionRecord(Long reservationId, String beforeStatus, String afterStatus,
                            java.time.LocalDateTime changedAt, String operatorUsername, String reason) {}
}
