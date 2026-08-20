/** 잔디심기(활동) API. 인증 필요(본인 활동). */
import client from './client';

/**
 * GET /api/v1/activity → [{ date:'YYYY-MM-DD', level:0~4, count }, ...] (오름차순)
 * 3단계 mockActivity.js 와 동일 shape 이라 GrassSection 렌더링은 그대로 사용한다.
 */
export function getActivity() {
  return client.get('/api/v1/activity');
}
