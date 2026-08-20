package com.solcho.bootcamp.activity.repository;

import com.solcho.bootcamp.activity.entity.ActivityLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Optional<ActivityLog> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);

    List<ActivityLog> findByUserIdAndActivityDateGreaterThanEqual(Long userId, LocalDate from);
}
