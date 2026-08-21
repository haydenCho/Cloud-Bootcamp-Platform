/** 학습 챕터 API. 조회는 공개, 방문 기록은 인증 필요. */
import client from './client';

/** GET /api/v1/units/{code}/chapters → [{ id, title, sortOrder }] */
export function getChapters(code) {
  return client.get(`/api/v1/units/${code}/chapters`);
}

/**
 * GET /api/v1/units/{code}/chapters/{sortOrder}
 * → { id, unitCode, title, sortOrder, body(HTML), prev, next }
 * prev/next = { sortOrder, title } | null
 */
export function getChapter(code, sortOrder) {
  return client.get(`/api/v1/units/${code}/chapters/${sortOrder}`);
}

/** POST /api/v1/chapters/{chapterId}/visit → 방문 기록(upsert). */
export function visitChapter(chapterId) {
  return client.post(`/api/v1/chapters/${chapterId}/visit`);
}
