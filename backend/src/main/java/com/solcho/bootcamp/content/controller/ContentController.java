package com.solcho.bootcamp.content.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.content.dto.ContentResponse;
import com.solcho.bootcamp.content.service.ContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 일반 학습 본문 조회. 인증 불필요(비로그인 학습 열람 허용).
 */
@RestController
@RequestMapping("/api/v1/units/{code}/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public ApiResponse<ContentResponse> getContent(@PathVariable String code) {
        return ApiResponse.ok(contentService.getByUnitCode(code));
    }
}
