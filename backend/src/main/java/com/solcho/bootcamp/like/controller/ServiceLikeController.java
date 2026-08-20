package com.solcho.bootcamp.like.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.UserPrincipal;
import com.solcho.bootcamp.like.dto.ServiceLikeResponse;
import com.solcho.bootcamp.like.service.ServiceLikeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서비스 좋아요. 조회는 공개(비로그인 총 카운트 열람), 토글은 인증 필요.
 */
@RestController
@RequestMapping("/api/v1/service-like")
public class ServiceLikeController {

    private final ServiceLikeService serviceLikeService;

    public ServiceLikeController(ServiceLikeService serviceLikeService) {
        this.serviceLikeService = serviceLikeService;
    }

    @GetMapping
    public ApiResponse<ServiceLikeResponse> status(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.id() : null;
        return ApiResponse.ok(serviceLikeService.getStatus(userId));
    }

    @PostMapping
    public ApiResponse<ServiceLikeResponse> toggle(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(serviceLikeService.toggle(principal.id()));
    }
}
