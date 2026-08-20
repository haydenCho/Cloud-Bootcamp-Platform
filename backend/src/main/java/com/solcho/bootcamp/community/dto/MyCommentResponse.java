package com.solcho.bootcamp.community.dto;

import java.time.LocalDateTime;

/** 대시보드 "내 커뮤니티 활동" — 내가 쓴 댓글(원 게시글로 이동하기 위한 postId/제목 포함). */
public record MyCommentResponse(
        Long id,
        String body,
        Long postId,
        String postTitle,
        Long parentCommentId,
        LocalDateTime createdAt
) {
}
