package com.solcho.bootcamp.activity.dto;

/**
 * 잔디심기 하루 칸. 프론트 mockActivity.js 반환 shape 과 동일: { date:'YYYY-MM-DD', level:0~4, count }.
 */
public record ActivityDayResponse(
        String date,
        int level,
        int count
) {
}
