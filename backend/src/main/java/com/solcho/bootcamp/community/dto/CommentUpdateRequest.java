package com.solcho.bootcamp.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 5000, message = "내용이 너무 깁니다.")
        String body
) {
}
