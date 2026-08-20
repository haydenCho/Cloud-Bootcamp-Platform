package com.solcho.bootcamp.blank.repository;

import com.solcho.bootcamp.blank.entity.BlankQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlankQuestionRepository extends JpaRepository<BlankQuestion, Long> {

    List<BlankQuestion> findByUnitIdOrderBySortOrderAsc(Long unitId);

    long countByUnitId(Long unitId);

    boolean existsByUnitId(Long unitId);

    /** 단원별 빈칸 문제 수 집계 (/study 카드 보조 정보용). Object[] = [unitId, count] */
    @Query("select q.unitId, count(q) from BlankQuestion q group by q.unitId")
    List<Object[]> countGroupedByUnit();
}
