package com.solcho.bootcamp.progress.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProgressUpdateRequest(
        @NotNull(message = "scrollPercent 는 필수입니다.")
        @Min(value = 0, message = "scrollPercent 는 0 이상이어야 합니다.")
        @Max(value = 100, message = "scrollPercent 는 100 이하여야 합니다.")
        Integer scrollPercent
) {
}
