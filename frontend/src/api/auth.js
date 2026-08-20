/** 인증 관련 API. 응답 인터셉터가 wrapper 를 벗겨 data 를 바로 반환한다. */
import client from './client';
import { setAuth, setAccessToken, clearAuth } from '../store/authStore';

/** 회원가입 → 바로 로그인 상태로. data: { accessToken, user } */
export async function signup({ loginId, password, nickname }) {
  const data = await client.post('/api/v1/auth/signup', { loginId, password, nickname });
  setAuth({ accessToken: data.accessToken, user: data.user });
  return data;
}

/** 로그인. data: { accessToken, user } */
export async function login({ loginId, password }) {
  const data = await client.post('/api/v1/auth/login', { loginId, password });
  setAuth({ accessToken: data.accessToken, user: data.user });
  return data;
}

/** 로그아웃. refresh 쿠키 무효화. */
export async function logout() {
  try {
    await client.post('/api/v1/auth/logout');
  } finally {
    clearAuth();
  }
}

/**
 * 앱 시작 시 refresh 쿠키로 세션 복구.
 * 성공 시 Access Token 을 메모리에 채우고 내 정보를 반환, 실패 시 비로그인 상태 유지.
 */
export async function bootstrapAuth() {
  try {
    const data = await client.post('/api/v1/auth/refresh');
    setAccessToken(data.accessToken);
    const me = await client.get('/api/v1/users/me');
    setAuth({ accessToken: data.accessToken, user: me });
    return me;
  } catch (_) {
    clearAuth();
    return null;
  }
}
