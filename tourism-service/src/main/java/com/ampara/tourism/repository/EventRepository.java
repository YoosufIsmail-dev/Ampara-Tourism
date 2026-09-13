package com.ampara.tourism.repository;

import com.ampara.tourism.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStartDateTimeAfterOrderByStartDateTimeAsc(LocalDateTime after);

    List<Event> findByStartDateTimeBetweenOrderByStartDateTimeAsc(LocalDateTime start, LocalDateTime end);

    List<Event> findByCategoryIgnoreCaseOrderByStartDateTimeAsc(String category);

    List<Event> findByTownIgnoreCaseOrderByStartDateTimeAsc(String town);

    List<Event> findByPlaceIdOrderByStartDateTimeAsc(Long placeId);
}
