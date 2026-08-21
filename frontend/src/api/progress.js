/** 학습 진도 API (인증 필요). */
import client from './client';

/**
 * GET /api/v1/progress → GENERAL 단원별 진도 요약 목록.
 * 각 항목: { unitCode, type:'GENERAL', generalPercent, blankPercent }
 * 8단계 개선: generalPercent 는 "방문 챕터 / 전체 챕터 × 100"(스크롤 아님).
 * 진도 기록은 챕터 방문(POST /chapters/{id}/visit)으로 이뤄지며 별도 저장 API 는 없다.
 */
export function getProgress() {
  return client.get('/api/v1/progress');
}
