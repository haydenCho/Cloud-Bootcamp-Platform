package com.solcho.bootcamp.community.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.community.dto.CommentCreateRequest;
import com.solcho.bootcamp.community.dto.CommentResponse;
import com.solcho.bootcamp.community.dto.CommentUpdateRequest;
import com.solcho.bootcamp.community.dto.MyCommentResponse;
import com.solcho.bootcamp.community.service.CommentService;
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
 * 커뮤니티 댓글/답글. 모두 인증 필요.
 */
@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/v1/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> list(@PathVariable Long postId) {
        return ApiResponse.ok(commentService.getTree(postId));
    }

    @PostMapping("/api/v1/posts/{postId}/comments")
    public ApiResponse<CommentResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest req) {
        return ApiResponse.ok(commentService.create(principal.id(), postId, req));
    }

    @PutMapping("/api/v1/comments/{id}")
    public ApiResponse<CommentResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequest req) {
        return ApiResponse.ok(commentService.update(principal.id(), principal.role(), id, req.body()));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ApiResponse<Object> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        commentService.delete(principal.id(), principal.role(), id);
        return ApiResponse.ok();
    }

    /** 대시보드 "내 커뮤니티 활동" — 내가 쓴 댓글. */
    @GetMapping("/api/v1/users/me/comments")
    public ApiResponse<List<MyCommentResponse>> myComments(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(commentService.getMyComments(principal.id()));
    }
}
