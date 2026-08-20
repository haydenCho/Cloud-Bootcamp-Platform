/**
 * 온보딩 투어 상태 (아주 단순한 관찰 가능 스토어).
 * - 회원가입 직후(자동 로그인)에만 startTour 로 시작한다. 로그인은 이 스토어를 건드리지 않는다.
 * - "봤는지" 여부는 백엔드가 아니라 localStorage 에 사용자별로 기록한다(브라우저 바꾸면 다시 뜸 — 허용 범위).
 */
import { useSyncExternalStore } from 'react';

let state = { active: false, step: 0 };
const listeners = new Set();

function emit() {
  for (const l of listeners) l();
}

export function getTourState() {
  return state;
}

export function startTour() {
  state = { active: true, step: 0 };
  emit();
}

export function goToStep(step) {
  state = { ...state, step };
  emit();
}

export function stopTour() {
  state = { active: false, step: 0 };
  emit();
}

function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function useTour() {
  return useSyncExternalStore(subscribe, getTourState);
}

const seenKey = (loginId) => `bootcamp_onboarding_seen_${loginId}`;

/** 회원가입 시 호출: 이 브라우저에서 아직 안 봤으면 투어 시작. */
export function maybeStartTour(loginId) {
  try {
    if (loginId && localStorage.getItem(seenKey(loginId))) return false;
  } catch (_) {
    /* localStorage 접근 불가 시 그냥 시작 */
  }
  startTour();
  return true;
}

/** 투어 종료/건너뛰기 시 호출: 이 사용자에 대해 봤음으로 기록. */
export function markTourSeen(loginId) {
  try {
    if (loginId) localStorage.setItem(seenKey(loginId), '1');
  } catch (_) {
    /* 무시 */
  }
}
