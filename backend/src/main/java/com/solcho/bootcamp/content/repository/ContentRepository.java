package com.solcho.bootcamp.content.repository;

import com.solcho.bootcamp.content.entity.Content;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContentRepository extends JpaRepository<Content, Long> {

    /** 단원의 대표 본문(가장 먼저 등록된 것). 4단계에서는 단원당 1개만 시드한다. */
    Optional<Content> findFirstByUnitIdOrderByIdAsc(Long unitId);

    boolean existsByUnitId(Long unitId);

    /** 본문이 하나라도 있는 단원 id 목록 (/study 카드 보조 정보용). */
    @Query("select distinct c.unitId from Content c")
    List<Long> findUnitIdsWithContent();
}
