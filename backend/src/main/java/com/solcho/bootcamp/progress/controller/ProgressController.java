package com.solcho.bootcamp.progress.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.UserPrincipal;
import com.solcho.bootcamp.progress.dto.UnitProgressSummary;
import com.solcho.bootcamp.progress.service.ProgressService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 학습 진도 조회. 인증 필요(anyRequest().authenticated()).
 * 8단계 개선: 스크롤 저장(PATCH)은 제거했고, 진도는 챕터 방문(POST /chapters/{id}/visit)으로 기록된다.
 */
@RestController
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /** 로그인 사용자의 GENERAL 단원별 진도 요약 (대시보드용). */
    @GetMapping("/api/v1/progress")
    public ApiResponse<List<UnitProgressSummary>> getProgress(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(progressService.getSummary(principal.id()));
    }
}
