package com.solcho.bootcamp.community.dto;

import java.time.LocalDateTime;

/** 게시글 목록 항목. 작성자 닉네임은 조회 시점에 조인해 채운다(복사 저장 안 함). */
public record PostSummaryResponse(
        Long id,
        String title,
        Long authorId,
        String authorNickname,
        int viewCount,
        long commentCount,
        LocalDateTime createdAt
) {
}
