/** 서비스 좋아요 API. 조회는 공개, 토글은 인증 필요. */
import client from './client';

/** GET /api/v1/service-like → { totalCount, likedByMe } */
export function getServiceLike() {
  return client.get('/api/v1/service-like');
}

/** POST /api/v1/service-like → 토글 후 { totalCount, likedByMe } */
export function toggleServiceLike() {
  return client.post('/api/v1/service-like');
}
