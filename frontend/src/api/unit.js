/** 학습 단원(로드맵) API. 공개 API 라 비로그인 상태에서도 호출된다. */
import client from './client';

/** 전체 단원 목록 (sort_order 순). */
export function getUnits() {
  return client.get('/api/v1/units');
}
