package com.solcho.bootcamp.blank.repository;

import com.solcho.bootcamp.blank.entity.BlankAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlankAnswerRepository extends JpaRepository<BlankAnswer, Long> {

    Optional<BlankAnswer> findByUserIdAndBlankQuestionId(Long userId, Long blankQuestionId);

    List<BlankAnswer> findByUserIdAndBlankQuestionIdIn(Long userId, List<Long> blankQuestionIds);

    /** 특정 사용자가 특정 단원의 빈칸 문제 중 맞힌 개수 (대시보드 blankPercent 계산용). */
    @Query("""
            select count(a) from BlankAnswer a
            where a.userId = :userId and a.isCorrect = true
              and a.blankQuestionId in (select q.id from BlankQuestion q where q.unitId = :unitId)
            """)
    long countCorrectByUserAndUnit(@Param("userId") Long userId, @Param("unitId") Long unitId);
}
