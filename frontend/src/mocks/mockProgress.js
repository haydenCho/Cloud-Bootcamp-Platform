/**
 * ⚠️ 더미(mock) 진도 데이터 — 실제 API 응답이 아닙니다.
 *
 * 3단계에서는 progress / blank_answer / mission_progress 테이블과 API를 아직 만들지 않으므로,
 * 화면을 채우기 위한 가짜 진도 값을 프론트에서 생성합니다.
 *
 * 4단계에서 실제 진도 API가 생기면, 이 파일의 getMockProgress() 를
 * 실제 API 호출(예: getProgress())로 교체하기만 하면 됩니다.
 * 컴포넌트는 아래 반환 구조에만 의존하도록 작성되어 있어 수정할 필요가 없습니다.
 *
 * 반환 구조 (unit.code 를 key 로 하는 map):
 *   GENERAL 단원 → { type:'GENERAL', generalPercent, blankPercent }
 *   PRACTICE 단원 → { type:'PRACTICE', practicePercent }
 * 각 percent 는 0~100 정수.
 */

/** 문자열 + salt 기반 결정론적 의사난수(0~100). 렌더링마다 값이 흔들리지 않도록 고정. */
function seededPercent(seed, salt) {
  let h = 2166136261 ^ salt;
  for (let i = 0; i < seed.length; i++) {
    h ^= seed.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  // 0~100, 5단위로 반올림
  const raw = (h >>> 0) % 101;
  const rounded = Math.round(raw / 5) * 5;
  // 85 이상은 100(완료)으로 스냅 → 일부 단원이 확실히 "완료 + glow" 상태가 되게 함
  return rounded >= 85 ? 100 : rounded;
}

/**
 * units 배열(GET /api/v1/units 응답)을 받아 code 별 더미 진도 map 을 만든다.
 * @param {Array<{code:string,type:'GENERAL'|'PRACTICE'}>} units
 * @returns {Record<string, object>}
 */
export function getMockProgress(units) {
  const map = {};
  for (const unit of units) {
    if (unit.type === 'PRACTICE') {
      map[unit.code] = {
        type: 'PRACTICE',
        practicePercent: seededPercent(unit.code, 7),
      };
    } else {
      map[unit.code] = {
        type: 'GENERAL',
        generalPercent: seededPercent(unit.code, 3),
        blankPercent: seededPercent(unit.code, 11),
      };
    }
  }
  return map;
}
