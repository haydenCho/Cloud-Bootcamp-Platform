/**
 * 인증 상태 저장소 (아주 단순한 관찰 가능 스토어).
 * - Access Token 은 메모리에만 보관한다. localStorage 저장 금지(XSS 위험) — CLAUDE.md 보안 원칙.
 * - Refresh Token 은 httpOnly 쿠키라 JS 에서 접근하지 않는다.
 * - 새로고침 시에는 /auth/refresh 로 Access Token 을 복구한다(bootstrapAuth 참고).
 */
import { useSyncExternalStore } from 'react';

let state = {
  accessToken: null,
  user: null,
};

const listeners = new Set();

function emit() {
  for (const l of listeners) l();
}

export function getAccessToken() {
  return state.accessToken;
}

export function getAuthState() {
  return state;
}

export function setAuth({ accessToken, user }) {
  state = { accessToken, user };
  emit();
}

export function setAccessToken(accessToken) {
  state = { ...state, accessToken };
  emit();
}

/** 토큰은 유지한 채 로그인 사용자 정보만 갱신 (예: 프로필 수정 후). */
export function setUser(user) {
  state = { ...state, user };
  emit();
}

export function clearAuth() {
  state = { accessToken: null, user: null };
  emit();
}

function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

/** React 컴포넌트에서 인증 상태를 구독한다. */
export function useAuth() {
  return useSyncExternalStore(subscribe, getAuthState);
}
