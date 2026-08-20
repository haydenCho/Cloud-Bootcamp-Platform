package com.solcho.bootcamp.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 게시글 작성/수정 요청 (본문은 일반 텍스트, 줄바꿈만 지원). */
public record PostCreateRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "본문을 입력해주세요.")
        @Size(max = 20000, message = "본문이 너무 깁니다.")
        String body
) {
}
