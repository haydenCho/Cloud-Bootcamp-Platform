import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  createComment,
  deleteComment,
  deletePost,
  getComments,
  getPost,
  updateComment,
} from '../../api/community';
import { useAuth } from '../../store/authStore';
import CommentItem from '../../components/community/CommentItem';
import { formatDate } from '../../components/community/format';

/** 게시글 상세 + 댓글/답글. */
export default function PostDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | done | error
  const [newComment, setNewComment] = useState('');
  const [posting, setPosting] = useState(false);

  const reloadComments = useCallback(() => {
    return getComments(id).then(setComments).catch(() => {});
  }, [id]);

  useEffect(() => {
    let alive = true;
    setStatus('loading');
    // 상세 조회(조회수 증가) + 댓글
    Promise.all([getPost(id), getComments(id)])
      .then(([p, c]) => {
        if (!alive) return;
        setPost(p);
        setComments(c);
        setStatus('done');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, [id]);

  async function handleDeletePost() {
    if (!window.confirm('이 게시글을 삭제할까요?')) return;
    await deletePost(id);
    navigate('/community');
  }

  async function handleCreateComment(body, parentCommentId = null) {
    await createComment(id, { body, parentCommentId });
    await reloadComments();
  }
  async function handleUpdateComment(commentId, body) {
    await updateComment(commentId, { body });
    await reloadComments();
  }
  async function handleDeleteComment(commentId) {
    if (!window.confirm('삭제할까요?')) return;
    await deleteComment(commentId);
    await reloadComments();
  }

  async function submitNewComment(e) {
    e.preventDefault();
    if (!newComment.trim()) return;
    setPosting(true);
    try {
      await handleCreateComment(newComment, null);
      setNewComment('');
    } finally {
      setPosting(false);
    }
  }

  if (status === 'loading') return <p className="py-16 text-center text-slate-400">불러오는 중...</p>;
  if (status === 'error') return <p className="py-16 text-center text-red-500">게시글을 불러오지 못했습니다.</p>;

  const isAuthor = user?.id === post.authorId;
  const isAdmin = user?.role === 'ADMIN';
  const commentCount = comments.reduce((n, c) => n + 1 + (c.replies?.length ?? 0), 0);

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <Link to="/community" className="text-sm text-secondary hover:text-primary">
        ← 목록
      </Link>

      {/* 게시글 */}
      <article className="mt-3 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-bold text-dark">{post.title}</h1>
        <div className="mt-2 flex items-center justify-between border-b border-slate-100 pb-3 text-sm text-dark/50">
          <span>{post.authorNickname}</span>
          <span>
            {formatDate(post.createdAt)} · 조회 {post.viewCount}
          </span>
        </div>
        <p className="mt-4 whitespace-pre-wrap break-words leading-relaxed text-dark/90">{post.body}</p>

        {(isAuthor || isAdmin) && (
          <div className="mt-6 flex justify-end gap-2">
            {isAuthor && (
              <Link
                to={`/community/${post.id}/edit`}
                className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm text-dark/70 transition hover:border-primary hover:text-primary"
              >
                수정
              </Link>
            )}
            {(isAuthor || isAdmin) && (
              <button
                onClick={handleDeletePost}
                className="rounded-lg border border-red-300 px-3 py-1.5 text-sm text-red-500 transition hover:bg-red-50"
              >
                삭제
              </button>
            )}
          </div>
        )}
      </article>

      {/* 댓글 */}
      <section className="mt-6">
        <h2 className="mb-2 text-lg font-bold text-dark">댓글 {commentCount}</h2>

        <div className="rounded-2xl border border-slate-200 bg-white px-5 shadow-sm">
          {comments.length === 0 ? (
            <p className="py-8 text-center text-sm text-dark/40">첫 댓글을 남겨보세요.</p>
          ) : (
            <div className="divide-y divide-slate-100">
              {comments.map((c) => (
                <CommentItem
                  key={c.id}
                  comment={c}
                  currentUser={user}
                  onReply={handleCreateComment}
                  onUpdate={handleUpdateComment}
                  onDelete={handleDeleteComment}
                />
              ))}
            </div>
          )}
        </div>

        {/* 새 댓글 작성 */}
        <form onSubmit={submitNewComment} className="mt-4">
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            rows={3}
            placeholder="댓글을 입력하세요"
            className="w-full resize-y rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-1 focus:ring-primary"
          />
          <div className="mt-2 flex justify-end">
            <button
              type="submit"
              disabled={posting}
              className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-light disabled:opacity-50"
            >
              댓글 등록
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
