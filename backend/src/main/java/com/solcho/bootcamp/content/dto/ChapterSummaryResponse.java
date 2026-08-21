package com.solcho.bootcamp.content.dto;

import com.solcho.bootcamp.content.entity.ContentChapter;

/** 챕터 목록 항목 (본문 제외). */
public record ChapterSummaryResponse(Long id, String title, int sortOrder) {
    public static ChapterSummaryResponse of(ContentChapter c) {
        return new ChapterSummaryResponse(c.getId(), c.getTitle(), c.getSortOrder());
    }
}
