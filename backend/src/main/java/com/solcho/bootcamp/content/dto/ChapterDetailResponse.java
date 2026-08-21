package com.solcho.bootcamp.content.dto;

import com.solcho.bootcamp.content.entity.ContentChapter;

/**
 * 챕터 본문 + 이전/다음 챕터 네비게이션.
 * prev/next 는 없으면 null.
 */
public record ChapterDetailResponse(
        Long id,
        String unitCode,
        String title,
        int sortOrder,
        String body,
        NavItem prev,
        NavItem next
) {
    public record NavItem(int sortOrder, String title) {}

    public static ChapterDetailResponse of(String unitCode, ContentChapter c, NavItem prev, NavItem next) {
        return new ChapterDetailResponse(
                c.getId(), unitCode, c.getTitle(), c.getSortOrder(), c.getBody(), prev, next);
    }
}
