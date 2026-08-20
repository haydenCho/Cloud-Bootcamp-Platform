package com.solcho.bootcamp.common.dto;

/**
 * 통일된 API 응답 wrapper.
 * 성공: { "success": true, "data": ..., "message": null }
 * 실패: { "success": false, "data": null, "message": "..." }
 */
public record ApiResponse<T>(boolean success, T data, String message) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
