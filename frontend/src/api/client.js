/**
 * axios 인스턴스 + 인터셉터.
 * - baseURL: VITE_API_BASE_URL
 * - withCredentials: true → refresh httpOnly 쿠키 전송
 * - 요청: 메모리의 Access Token 을 Authorization 헤더로 첨부
 * - 응답: 공통 wrapper({success,data,message}) 를 벗겨 data 만 반환. 실패면 message 로 throw.
 * - 401: /auth/refresh 로 한 번 재발급 후 원 요청 재시도.
 */
import axios from 'axios';
import { getAccessToken, setAccessToken, clearAuth } from '../store/authStore';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const client = axios.create({
  baseURL,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

// refresh 전용 인스턴스 (인터셉터 재귀 방지)
const refreshClient = axios.create({ baseURL, withCredentials: true });

client.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshPromise = null;

function requestRefresh() {
  if (!refreshPromise) {
    refreshPromise = refreshClient
      .post('/api/v1/auth/refresh')
      .then((res) => res.data?.data?.accessToken ?? null)
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

client.interceptors.response.use(
  (response) => {
    // 공통 wrapper 처리
    const body = response.data;
    if (body && typeof body === 'object' && 'success' in body) {
      if (body.success) return body.data;
      return Promise.reject(new Error(body.message || '요청에 실패했습니다.'));
    }
    return body;
  },
  async (error) => {
    const original = error.config;
    const status = error.response?.status;
    const isRefreshCall = original?.url?.includes('/api/v1/auth/refresh');

    // 401 → 한 번만 재발급 시도 후 재요청
    if (status === 401 && original && !original._retry && !isRefreshCall) {
      original._retry = true;
      try {
        const newToken = await requestRefresh();
        if (newToken) {
          setAccessToken(newToken);
          original.headers = original.headers || {};
          original.headers.Authorization = `Bearer ${newToken}`;
          return client(original);
        }
      } catch (_) {
        // 재발급 실패 → 로그아웃 처리
      }
      clearAuth();
    }

    const message =
      error.response?.data?.message || error.message || '네트워크 오류가 발생했습니다.';
    return Promise.reject(new Error(message));
  },
);

export default client;
