package com.solcho.bootcamp.progress.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.UserPrincipal;
import com.solcho.bootcamp.progress.dto.ProgressResponse;
import com.solcho.bootcamp.progress.dto.ProgressUpdateRequest;
import com.solcho.bootcamp.progress.dto.UnitProgressSummary;
import com.solcho.bootcamp.progress.service.ProgressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 학습 진도 저장/조회. 모두 인증 필요(SecurityConfig 에서 GET /units/** 만 공개이고
 * PATCH progress / GET progress 는 anyRequest authenticated 에 해당).
 */
@RestController
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /** 스크롤 진도 저장 (upsert). */
    @PatchMapping("/api/v1/units/{code}/progress")
    public ApiResponse<ProgressResponse> updateProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code,
            @Valid @RequestBody ProgressUpdateRequest req) {
        return ApiResponse.ok(progressService.updateScroll(principal.id(), code, req.scrollPercent()));
    }

    /** 로그인 사용자의 GENERAL 단원별 진도 요약 (대시보드용). */
    @GetMapping("/api/v1/progress")
    public ApiResponse<List<UnitProgressSummary>> getProgress(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(progressService.getSummary(principal.id()));
    }
}
