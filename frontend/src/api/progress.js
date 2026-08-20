/** 학습 진도 API (인증 필요). */
import client from './client';

/**
 * GET /api/v1/progress → GENERAL 단원별 진도 요약 목록.
 * 각 항목: { unitCode, type:'GENERAL', generalPercent, blankPercent }
 * (3단계 mockProgress 의 GENERAL shape 과 동일하므로 대시보드에서 그대로 대체 가능)
 */
export function getProgress() {
  return client.get('/api/v1/progress');
}

/** PATCH /api/v1/units/{code}/progress → { unitCode, scrollPercent, completed } */
export function updateProgress(code, scrollPercent) {
  return client.patch(`/api/v1/units/${code}/progress`, { scrollPercent });
}
