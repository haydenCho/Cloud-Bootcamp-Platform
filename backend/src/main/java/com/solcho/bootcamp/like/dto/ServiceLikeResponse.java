package com.solcho.bootcamp.like.dto;

/** 서비스 좋아요 상태. likedByMe 는 비로그인 시 항상 false. */
public record ServiceLikeResponse(
        long totalCount,
        boolean likedByMe
) {
}
