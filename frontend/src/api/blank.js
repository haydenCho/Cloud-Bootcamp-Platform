/** 빈칸 채우기 API. 목록 조회는 공개, 답안 제출은 인증 필요. */
import client from './client';

/**
 * GET /api/v1/units/{code}/blanks → 문제 목록.
 * 각 항목: { id, sentenceTemplate, score, sortOrder, userAnswer|null, isCorrect|null }
 * (정답 문자열은 목록에 포함되지 않는다 — 채점 응답에서만 노출)
 */
export function getBlanks(code) {
  return client.get(`/api/v1/units/${code}/blanks`);
}

/** POST /api/v1/blanks/{id}/answer → { isCorrect, correctAnswer } */
export function submitBlankAnswer(id, answer) {
  return client.post(`/api/v1/blanks/${id}/answer`, { answer });
}
