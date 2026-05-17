package org.example.userservice.repository;

import org.example.userservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface UserEventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUserIdAndStartDateTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<Event> findByUserIdOrderByStartDateTimeAsc(Long userId);

    @Query("SELECT e FROM Event e WHERE e.userId = :userId AND YEAR(e.startDateTime) = :year AND MONTH(e.startDateTime) = :month")
    List<Event> findByUserIdAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    void deleteByUserId(Long userId);
}
