package com.ampara.tourism.repository;

import com.ampara.tourism.entity.Booking;
import com.ampara.tourism.entity.BookingStatus;
import com.ampara.tourism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);

    @Query("select count(b) > 0 from Booking b where b.room.id = :roomId " +
           "and b.status = :status and b.checkIn < :checkOut and b.checkOut > :checkIn")
    boolean existsOverlappingBooking(@Param("roomId") Long roomId,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut,
                                     @Param("status") BookingStatus status);
}
