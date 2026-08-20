package com.solcho.bootcamp.mission.repository;

import com.solcho.bootcamp.mission.entity.MissionProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionProgressRepository extends JpaRepository<MissionProgress, Long> {

    Optional<MissionProgress> findByUserIdAndMissionId(Long userId, Long missionId);

    List<MissionProgress> findByUserIdAndMissionIdIn(Long userId, List<Long> missionIds);
}
