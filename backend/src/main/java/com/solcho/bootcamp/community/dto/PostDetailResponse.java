package com.solcho.bootcamp.community.dto;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long id,
        String title,
        String body,
        Long authorId,
        String authorNickname,
        String authorProfileImageUrl,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
