package com.solcho.bootcamp.content.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.UserPrincipal;
import com.solcho.bootcamp.content.dto.ChapterDetailResponse;
import com.solcho.bootcamp.content.dto.ChapterSummaryResponse;
import com.solcho.bootcamp.content.service.ChapterService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 학습 챕터 조회(공개) + 방문 기록(인증).
 * - GET /api/v1/units/{unitCode}/chapters                 : 챕터 목록
 * - GET /api/v1/units/{unitCode}/chapters/{sortOrder}     : 챕터 본문 + 이전/다음
 * - POST /api/v1/chapters/{chapterId}/visit               : 방문 기록(로그인)
 * 조회 GET 은 SecurityConfig 의 GET /api/v1/units/** 공개 규칙에 해당.
 * POST visit 은 /api/v1/chapters/** 로 anyRequest().authenticated() 에 해당.
 */
@RestController
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping("/api/v1/units/{unitCode}/chapters")
    public ApiResponse<List<ChapterSummaryResponse>> listChapters(@PathVariable String unitCode) {
        return ApiResponse.ok(chapterService.listChapters(unitCode));
    }

    @GetMapping("/api/v1/units/{unitCode}/chapters/{sortOrder}")
    public ApiResponse<ChapterDetailResponse> getChapter(
            @PathVariable String unitCode,
            @PathVariable int sortOrder) {
        return ApiResponse.ok(chapterService.getChapter(unitCode, sortOrder));
    }

    @PostMapping("/api/v1/chapters/{chapterId}/visit")
    public ApiResponse<Object> visit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long chapterId) {
        chapterService.visit(principal.id(), chapterId);
        return ApiResponse.ok();
    }
}
