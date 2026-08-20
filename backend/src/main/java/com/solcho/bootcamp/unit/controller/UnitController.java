package com.solcho.bootcamp.unit.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.unit.dto.UnitResponse;
import com.solcho.bootcamp.unit.service.UnitService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 학습 단원(로드맵) 조회. 인증 없이 접근 가능(SecurityConfig 의 공개 GET 목록).
 */
@RestController
@RequestMapping("/api/v1/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    public ApiResponse<List<UnitResponse>> getUnits() {
        return ApiResponse.ok(unitService.getAllUnits());
    }
}
