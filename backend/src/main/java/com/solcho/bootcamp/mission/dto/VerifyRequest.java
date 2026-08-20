package com.solcho.bootcamp.mission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerifyRequest(
        @NotNull(message = "input 은 필수입니다.")
        @Size(max = 500, message = "입력이 너무 깁니다.")
        String input
) {
}
