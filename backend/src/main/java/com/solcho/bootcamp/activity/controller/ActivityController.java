package com.solcho.bootcamp.activity.controller;

import com.solcho.bootcamp.activity.dto.ActivityDayResponse;
import com.solcho.bootcamp.activity.service.ActivityLogService;
import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.UserPrincipal;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 잔디심기 조회 (인증 필요 — 본인 활동).
 */
@RestController
@RequestMapping("/api/v1/activity")
public class ActivityController {

    private final ActivityLogService activityLogService;

    public ActivityController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ApiResponse<List<ActivityDayResponse>> getActivity(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(activityLogService.getRecentActivity(principal.id()));
    }
}
