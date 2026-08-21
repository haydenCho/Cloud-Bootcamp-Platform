package com.solcho.bootcamp.progress.repository;

import com.solcho.bootcamp.progress.entity.ChapterProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChapterProgressRepository extends JpaRepository<ChapterProgress, Long> {

    boolean existsByUserIdAndChapterId(Long userId, Long chapterId);

    /** 특정 content(단원 본문)에 속한 챕터들 중 사용자가 방문한 챕터 수. */
    @Query("select count(cp) from ChapterProgress cp "
            + "where cp.userId = :userId and cp.chapterId in "
            + "(select c.id from ContentChapter c where c.contentId = :contentId)")
    long countVisitedByUserAndContent(@Param("userId") Long userId,
                                      @Param("contentId") Long contentId);
}
