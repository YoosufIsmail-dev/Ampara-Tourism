package com.ampara.tourism.repository;

import com.ampara.tourism.entity.HotelRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HotelRoomRepository extends JpaRepository<HotelRoom, Long> {
    List<HotelRoom> findByHotelId(Long hotelId);
    List<HotelRoom> findByHotelIdIn(List<Long> hotelIds);
    List<HotelRoom> findByHotelIdAndAvailableTrue(Long hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from HotelRoom r where r.id = :id")
    java.util.Optional<HotelRoom> findByIdForUpdate(@Param("id") Long id);
}
