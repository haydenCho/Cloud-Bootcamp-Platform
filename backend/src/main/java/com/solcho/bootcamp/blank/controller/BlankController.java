package com.solcho.bootcamp.blank.controller;

import com.solcho.bootcamp.blank.dto.BlankQuestionResponse;
import com.solcho.bootcamp.blank.dto.SubmitAnswerRequest;
import com.solcho.bootcamp.blank.dto.SubmitAnswerResponse;
import com.solcho.bootcamp.blank.service.BlankService;
import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlankController {

    private final BlankService blankService;

    public BlankController(BlankService blankService) {
        this.blankService = blankService;
    }

    /**
     * 단원 빈칸 문제 목록 (공개 조회). 로그인 상태면 이전 답안/정답 여부도 함께 반환한다.
     * principal 은 비로그인 시 null 이다(경로가 GET /units/** 라 인증이 필수는 아니지만,
     * JWT 필터는 항상 동작하므로 유효 토큰이 있으면 주입된다).
     */
    @GetMapping("/api/v1/units/{code}/blanks")
    public ApiResponse<List<BlankQuestionResponse>> getBlanks(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        Long userId = principal != null ? principal.id() : null;
        return ApiResponse.ok(blankService.getBlanks(code, userId));
    }

    /** 답안 제출/채점 (인증 필요). */
    @PostMapping("/api/v1/blanks/{id}/answer")
    public ApiResponse<SubmitAnswerResponse> submitAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SubmitAnswerRequest req) {
        return ApiResponse.ok(blankService.submitAnswer(principal.id(), id, req.answer()));
    }
}
