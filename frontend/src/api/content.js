/** 일반 학습 본문 API (공개). */
import client from './client';

/** GET /api/v1/units/{code}/content → { id, unitCode, title, body(HTML) } */
export function getContent(code) {
  return client.get(`/api/v1/units/${code}/content`);
}
