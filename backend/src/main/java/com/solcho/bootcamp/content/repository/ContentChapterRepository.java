package com.solcho.bootcamp.content.repository;

import com.solcho.bootcamp.content.entity.ContentChapter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentChapterRepository extends JpaRepository<ContentChapter, Long> {

    List<ContentChapter> findByContentIdOrderBySortOrderAsc(Long contentId);

    Optional<ContentChapter> findByContentIdAndSortOrder(Long contentId, int sortOrder);

    long countByContentId(Long contentId);

    void deleteByContentId(Long contentId);
}
