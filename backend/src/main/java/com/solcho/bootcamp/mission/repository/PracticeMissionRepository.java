package com.solcho.bootcamp.mission.repository;

import com.solcho.bootcamp.mission.entity.PracticeMission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PracticeMissionRepository extends JpaRepository<PracticeMission, Long> {

    List<PracticeMission> findByUnitIdOrderBySortOrderAsc(Long unitId);

    boolean existsByUnitId(Long unitId);

    /** 단원별 실습 미션 수 집계 (/study 카드 보조 정보용). Object[] = [unitId, count] */
    @Query("select m.unitId, count(m) from PracticeMission m group by m.unitId")
    List<Object[]> countGroupedByUnit();
}
