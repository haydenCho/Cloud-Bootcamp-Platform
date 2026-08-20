package com.solcho.bootcamp.blank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitAnswerRequest(
        @NotNull(message = "answer 는 필수입니다.")
        @Size(max = 200, message = "답안이 너무 깁니다.")
        String answer
) {
}
