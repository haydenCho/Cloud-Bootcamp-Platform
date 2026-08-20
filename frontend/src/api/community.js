/** 커뮤니티(게시글/댓글) API. 모두 인증 필요. */
import client from './client';

// ---- 게시글 ----
export function getPosts() {
  return client.get('/api/v1/posts');
}
export function getPost(id) {
  return client.get(`/api/v1/posts/${id}`);
}
export function createPost({ title, body }) {
  return client.post('/api/v1/posts', { title, body });
}
export function updatePost(id, { title, body }) {
  return client.put(`/api/v1/posts/${id}`, { title, body });
}
export function deletePost(id) {
  return client.delete(`/api/v1/posts/${id}`);
}

// ---- 댓글/답글 ----
export function getComments(postId) {
  return client.get(`/api/v1/posts/${postId}/comments`);
}
export function createComment(postId, { body, parentCommentId }) {
  return client.post(`/api/v1/posts/${postId}/comments`, { body, parentCommentId });
}
export function updateComment(id, { body }) {
  return client.put(`/api/v1/comments/${id}`, { body });
}
export function deleteComment(id) {
  return client.delete(`/api/v1/comments/${id}`);
}

// ---- 대시보드 "내 커뮤니티 활동" ----
export function getMyPosts() {
  return client.get('/api/v1/users/me/posts');
}
export function getMyComments() {
  return client.get('/api/v1/users/me/comments');
}
