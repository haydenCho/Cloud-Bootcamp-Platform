import { useState } from 'react';
import { formatDate } from './format';

/**
 * 댓글 1개(및 답글). 1단계 깊이만 렌더링한다.
 * 권한 UI: 수정 = 작성자 본인, 삭제 = 작성자 본인 또는 ADMIN.
 * (최종 검증은 백엔드)
 */
export default function CommentItem({ comment, currentUser, isReply = false, onReply, onUpdate, onDelete }) {
  const [editing, setEditing] = useState(false);
  const [editValue, setEditValue] = useState(comment.body);
  const [replying, setReplying] = useState(false);
  const [replyValue, setReplyValue] = useState('');
  const [busy, setBusy] = useState(false);

  const isAuthor = currentUser?.id === comment.authorId;
  const isAdmin = currentUser?.role === 'ADMIN';
  const canEdit = isAuthor;
  const canDelete = isAuthor || isAdmin;

  async function submitEdit() {
    if (!editValue.trim()) return;
    setBusy(true);
    try {
      await onUpdate(comment.id, editValue);
      setEditing(false);
    } finally {
      setBusy(false);
    }
  }

  async function submitReply() {
    if (!replyValue.trim()) return;
    setBusy(true);
    try {
      await onReply(replyValue, comment.id);
      setReplyValue('');
      setReplying(false);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className={isReply ? 'ml-8 border-l-2 border-slate-100 pl-4' : ''}>
      <div className="py-3">
        <div className="flex items-center justify-between">
          <span className="text-sm font-semibold text-dark">
            {isReply && <span className="mr-1 text-secondary">↳</span>}
            {comment.authorNickname}
          </span>
          <span className="text-xs text-dark/40">{formatDate(comment.createdAt)}</span>
        </div>

        {editing ? (
          <div className="mt-2">
            <textarea
              value={editValue}
              onChange={(e) => setEditValue(e.target.value)}
              rows={3}
              className="w-full resize-y rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary"
            />
            <div className="mt-1 flex gap-2">
              <button
                onClick={submitEdit}
                disabled={busy}
                className="rounded-md bg-primary px-3 py-1 text-xs font-semibold text-white hover:bg-light disabled:opacity-50"
              >
                저장
              </button>
              <button
                onClick={() => {
                  setEditing(false);
                  setEditValue(comment.body);
                }}
                className="rounded-md border border-slate-300 px-3 py-1 text-xs text-dark/60"
              >
                취소
              </button>
            </div>
          </div>
        ) : (
          <p className="mt-1 whitespace-pre-wrap break-words text-sm text-dark/90">{comment.body}</p>
        )}

        {!editing && (
          <div className="mt-1 flex gap-3 text-xs text-dark/40">
            {!isReply && (
              <button onClick={() => setReplying((v) => !v)} className="hover:text-primary">
                답글
              </button>
            )}
            {canEdit && (
              <button onClick={() => setEditing(true)} className="hover:text-primary">
                수정
              </button>
            )}
            {canDelete && (
              <button
                onClick={() => onDelete(comment.id)}
                className="hover:text-red-500"
              >
                삭제
              </button>
            )}
          </div>
        )}

        {replying && (
          <div className="mt-2">
            <textarea
              value={replyValue}
              onChange={(e) => setReplyValue(e.target.value)}
              rows={2}
              placeholder="답글을 입력하세요"
              className="w-full resize-y rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary"
            />
            <div className="mt-1 flex gap-2">
              <button
                onClick={submitReply}
                disabled={busy}
                className="rounded-md bg-primary px-3 py-1 text-xs font-semibold text-white hover:bg-light disabled:opacity-50"
              >
                답글 등록
              </button>
              <button
                onClick={() => setReplying(false)}
                className="rounded-md border border-slate-300 px-3 py-1 text-xs text-dark/60"
              >
                취소
              </button>
            </div>
          </div>
        )}
      </div>

      {/* 답글 목록 (1단계) */}
      {comment.replies?.map((r) => (
        <CommentItem
          key={r.id}
          comment={r}
          currentUser={currentUser}
          isReply
          onUpdate={onUpdate}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
}
