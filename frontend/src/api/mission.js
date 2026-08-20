/** 실습 미션 API. 목록 조회는 공개, 검증은 인증 필요. */
import client from './client';

/**
 * GET /api/v1/units/{code}/missions
 * → { missions:[{id,title,description,missionType,xpReward,sortOrder,completed}], earnedXp, totalXp }
 * (verify_pattern 은 응답에 포함되지 않는다)
 */
export function getMissions(code) {
  return client.get(`/api/v1/units/${code}/missions`);
}

/** POST /api/v1/missions/{id}/verify → { correct, completed, xpReward } */
export function verifyMission(id, input) {
  return client.post(`/api/v1/missions/${id}/verify`, { input });
}
