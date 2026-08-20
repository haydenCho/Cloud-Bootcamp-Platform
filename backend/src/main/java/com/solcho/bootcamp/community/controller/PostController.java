package com.solcho.bootcamp.community.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.community.dto.PostCreateRequest;
import com.solcho.bootcamp.community.dto.PostDetailResponse;
import com.solcho.bootcamp.community.dto.PostSummaryResponse;
import com.solcho.bootcamp.community.service.PostService;
import com.solcho.bootcamp.config.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 커뮤니티 게시글. 열람·작성 모두 인증 필요(SecurityConfig 의 authenticated 대상).
 */
@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/v1/posts")
    public ApiResponse<List<PostSummaryResponse>> list() {
        return ApiResponse.ok(postService.list());
    }

    @PostMapping("/api/v1/posts")
    public ApiResponse<PostDetailResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PostCreateRequest req) {
        return ApiResponse.ok(postService.create(principal.id(), req));
    }

    @GetMapping("/api/v1/posts/{id}")
    public ApiResponse<PostDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(postService.getDetail(id));
    }

    @PutMapping("/api/v1/posts/{id}")
    public ApiResponse<PostDetailResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody PostCreateRequest req) {
        return ApiResponse.ok(postService.update(principal.id(), id, req));
    }

    @DeleteMapping("/api/v1/posts/{id}")
    public ApiResponse<Object> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        postService.delete(principal.id(), principal.role(), id);
        return ApiResponse.ok();
    }

    /** 대시보드 "내 커뮤니티 활동" — 내가 쓴 글. */
    @GetMapping("/api/v1/users/me/posts")
    public ApiResponse<List<PostSummaryResponse>> myPosts(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(postService.getMyPosts(principal.id()));
    }
}
