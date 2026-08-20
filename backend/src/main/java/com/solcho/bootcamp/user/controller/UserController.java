package com.solcho.bootcamp.user.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.UserPrincipal;
import com.solcho.bootcamp.user.dto.ChangePasswordRequest;
import com.solcho.bootcamp.user.dto.UpdateProfileRequest;
import com.solcho.bootcamp.user.dto.UserResponse;
import com.solcho.bootcamp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인한 사용자 본인 기준 CRUD. 모든 엔드포인트는 인증 필요(SecurityConfig 에서 anyRequest authenticated).
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(UserResponse.from(userService.getById(principal.id())));
    }

    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                   @Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(UserResponse.from(userService.updateProfile(principal.id(), req)));
    }

    @PatchMapping("/me/password")
    public ApiResponse<Object> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(principal.id(), req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/me")
    public ApiResponse<Object> deleteAccount(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteAccount(principal.id());
        return ApiResponse.ok();
    }
}
