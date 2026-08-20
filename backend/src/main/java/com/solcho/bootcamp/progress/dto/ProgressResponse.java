package com.solcho.bootcamp.progress.dto;

import com.solcho.bootcamp.progress.entity.Progress;

/** PATCH .../progress 응답 (해당 단원 1개의 현재 진도). */
public record ProgressResponse(
        String unitCode,
        int scrollPercent,
        boolean completed
) {
    public static ProgressResponse of(String unitCode, Progress progress) {
        return new ProgressResponse(unitCode, progress.getScrollPercent(), progress.isCompleted());
    }
}
