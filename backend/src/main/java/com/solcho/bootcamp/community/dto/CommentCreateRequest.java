package com.solcho.bootcamp.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글/답글 작성. parentCommentId 가 있으면 그 댓글에 대한 답글(1단계 깊이만 허용). */
public record CommentCreateRequest(
        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 5000, message = "내용이 너무 깁니다.")
        String body,

        Long parentCommentId
) {
}
