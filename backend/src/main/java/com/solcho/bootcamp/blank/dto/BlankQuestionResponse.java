package com.solcho.bootcamp.blank.dto;

/**
 * 빈칸 문제 목록 응답 항목.
 * ⚠️ 정답(answer)은 포함하지 않는다(치팅 방지). 정답은 채점 응답(POST)에서만 노출.
 * userAnswer / isCorrect 는 로그인 상태에서 이전 답안이 있을 때만 채워지고, 없으면 null.
 */
public record BlankQuestionResponse(
        Long id,
        String sentenceTemplate,
        int score,
        int sortOrder,
        String userAnswer,
        Boolean isCorrect
) {
}
