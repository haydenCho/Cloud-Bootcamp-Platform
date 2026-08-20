/** 사용자(본인) CRUD API. */
import client from './client';

export function getMe() {
  return client.get('/api/v1/users/me');
}

export function updateProfile({ nickname, profileImageUrl }) {
  return client.patch('/api/v1/users/me', { nickname, profileImageUrl });
}

export function changePassword({ currentPassword, newPassword }) {
  return client.patch('/api/v1/users/me/password', { currentPassword, newPassword });
}

export function deleteAccount() {
  return client.delete('/api/v1/users/me');
}
