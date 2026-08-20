package com.solcho.bootcamp.community.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 트리 항목. 최상위 댓글은 replies 에 답글 목록을 담고, 답글은 replies 가 빈 리스트(1단계 깊이).
 */
public record CommentResponse(
        Long id,
        String body,
        Long authorId,
        String authorNickname,
        String authorProfileImageUrl,
        Long parentCommentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> replies
) {
}
