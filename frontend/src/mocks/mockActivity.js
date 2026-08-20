/**
 * ⚠️ 더미(mock) 활동 데이터 — 실제 API 응답이 아닙니다.
 *
 * 3단계에서는 activity_log 테이블/API를 아직 만들지 않으므로, 잔디심기 그래프를 채우기 위한
 * 가짜 날짜별 활동량을 프론트에서 생성합니다.
 *
 * 4단계에서 실제 activity_log API가 생기면 getMockActivity() 를 실제 호출로 교체하면 됩니다.
 * (반환 구조: [{ date:'YYYY-MM-DD', count:number, level:0~4 }, ...] 오름차순)
 */

function pad2(n) {
  return String(n).padStart(2, '0');
}

function toDateStr(d) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;
}

/** 날짜 문자열 기반 결정론적 활동량(0~4 level). 렌더링마다 고정. */
function seededLevel(dateStr) {
  let h = 2166136261;
  for (let i = 0; i < dateStr.length; i++) {
    h ^= dateStr.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  const v = (h >>> 0) % 100;
  // 약 45%는 활동 없음(0), 나머지는 1~4로 분포
  if (v < 45) return 0;
  if (v < 65) return 1;
  if (v < 82) return 2;
  if (v < 94) return 3;
  return 4;
}

/** level → 대략적인 활동 횟수(툴팁 표시용). */
function levelToCount(level) {
  return [0, 1, 3, 6, 10][level];
}

/**
 * 오늘로부터 과거 weeks 주 만큼의 일별 활동 더미를 생성한다.
 * 시작일은 일요일에 정렬해 GitHub 스타일 주 단위 그리드에 딱 맞춘다.
 * @param {number} weeks 주 수 (기본 26주 ≈ 6개월)
 */
export function getMockActivity(weeks = 26) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 이번 주 일요일(그리드 마지막 열의 시작)
  const lastSunday = new Date(today);
  lastSunday.setDate(today.getDate() - today.getDay());

  // 시작 일요일 = 마지막 일요일에서 (weeks-1)주 전
  const start = new Date(lastSunday);
  start.setDate(lastSunday.getDate() - (weeks - 1) * 7);

  const days = [];
  const cursor = new Date(start);
  while (cursor <= today) {
    const dateStr = toDateStr(cursor);
    const level = seededLevel(dateStr);
    days.push({ date: dateStr, level, count: levelToCount(level) });
    cursor.setDate(cursor.getDate() + 1);
  }
  return days;
}
