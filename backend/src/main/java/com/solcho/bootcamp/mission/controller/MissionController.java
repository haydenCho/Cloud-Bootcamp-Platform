package com.solcho.bootcamp.mission.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.UserPrincipal;
import com.solcho.bootcamp.mission.dto.MissionListResponse;
import com.solcho.bootcamp.mission.dto.VerifyRequest;
import com.solcho.bootcamp.mission.dto.VerifyResponse;
import com.solcho.bootcamp.mission.service.MissionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    /** PRACTICE 단원의 미션 목록 (공개). 로그인 시 완료 여부/획득 XP 포함. */
    @GetMapping("/api/v1/units/{code}/missions")
    public ApiResponse<MissionListResponse> getMissions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String code) {
        Long userId = principal != null ? principal.id() : null;
        return ApiResponse.ok(missionService.getMissions(code, userId));
    }

    /** 미션 정답 검증 (인증 필요). 실제 실행 없이 패턴 검증만 수행. */
    @PostMapping("/api/v1/missions/{id}/verify")
    public ApiResponse<VerifyResponse> verify(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody VerifyRequest req) {
        return ApiResponse.ok(missionService.verify(principal.id(), id, req.input()));
    }
}
