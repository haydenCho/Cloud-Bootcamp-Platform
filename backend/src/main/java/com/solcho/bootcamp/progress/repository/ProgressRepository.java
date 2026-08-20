package com.solcho.bootcamp.progress.repository;

import com.solcho.bootcamp.progress.entity.Progress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByUserIdAndUnitId(Long userId, Long unitId);

    List<Progress> findByUserId(Long userId);
}
