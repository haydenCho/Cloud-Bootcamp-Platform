package com.solcho.bootcamp.blank.dto;

/** 채점 응답. 틀렸을 때 정답을 회색 글씨로 보여줄 수 있도록 correctAnswer 를 함께 반환한다. */
public record SubmitAnswerResponse(
        boolean isCorrect,
        String correctAnswer
) {
}
