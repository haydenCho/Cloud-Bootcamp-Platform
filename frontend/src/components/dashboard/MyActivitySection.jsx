import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteComment, deletePost, getMyComments, getMyPosts } from '../../api/community';
import { formatDate } from '../community/format';

/**
 * 대시보드 "내 커뮤니티 활동" — 내가 쓴 글/댓글.
 * 글: 상세 이동 + 수정 링크 + 삭제. 댓글: 원글 이동 + 삭제(수정은 원글 상세에서).
 */
export default function MyActivitySection() {
  const [posts, setPosts] = useState([]);
  const [comments, setComments] = useState([]);
  const [status, setStatus] = useState('loading');

  function load() {
    return Promise.all([getMyPosts(), getMyComments()])
      .then(([p, c]) => {
        setPosts(p);
        setComments(c);
        setStatus('done');
      })
      .catch(() => setStatus('error'));
  }

  useEffect(() => {
    load();
  }, []);

  async function handleDeletePost(id) {
    if (!window.confirm('이 게시글을 삭제할까요?')) return;
    await deletePost(id);
    await load();
  }
  async function handleDeleteComment(id) {
    if (!window.confirm('이 댓글을 삭제할까요?')) return;
    await deleteComment(id);
    await load();
  }

  if (status === 'loading') return <p className="py-10 text-center text-slate-400">불러오는 중...</p>;
  if (status === 'error')
    return <p className="py-10 text-center text-red-500">활동 내역을 불러오지 못했습니다.</p>;

  return (
    <section className="space-y-8">
      {/* 내가 쓴 글 */}
      <div>
        <h2 className="mb-3 text-xl font-bold text-dark">내가 쓴 글 ({posts.length})</h2>
        {posts.length === 0 ? (
          <p className="rounded-xl border border-slate-200 bg-white p-5 text-sm text-dark/50">
            작성한 글이 없습니다.
          </p>
        ) : (
          <ul className="divide-y divide-slate-100 rounded-2xl border border-slate-200 bg-white shadow-sm">
            {posts.map((p) => (
              <li key={p.id} className="flex items-center justify-between gap-3 px-5 py-3">
                <Link to={`/community/${p.id}`} className="min-w-0 flex-1">
                  <p className="truncate font-medium text-dark">{p.title}</p>
                  <p className="text-xs text-dark/50">
                    {formatDate(p.createdAt)} · 조회 {p.viewCount} · 댓글 {p.commentCount}
                  </p>
                </Link>
                <div className="flex shrink-0 gap-2 text-xs">
                  <Link
                    to={`/community/${p.id}/edit`}
                    className="rounded-md border border-slate-300 px-2 py-1 text-dark/60 hover:border-primary hover:text-primary"
                  >
                    수정
                  </Link>
                  <button
                    onClick={() => handleDeletePost(p.id)}
                    className="rounded-md border border-red-300 px-2 py-1 text-red-500 hover:bg-red-50"
                  >
                    삭제
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* 내가 쓴 댓글 */}
      <div>
        <h2 className="mb-3 text-xl font-bold text-dark">내가 쓴 댓글 ({comments.length})</h2>
        {comments.length === 0 ? (
          <p className="rounded-xl border border-slate-200 bg-white p-5 text-sm text-dark/50">
            작성한 댓글이 없습니다.
          </p>
        ) : (
          <ul className="divide-y divide-slate-100 rounded-2xl border border-slate-200 bg-white shadow-sm">
            {comments.map((c) => (
              <li key={c.id} className="flex items-center justify-between gap-3 px-5 py-3">
                <Link to={`/community/${c.postId}`} className="min-w-0 flex-1">
                  <p className="truncate text-sm text-dark/90">
                    {c.parentCommentId && <span className="mr-1 text-secondary">↳</span>}
                    {c.body}
                  </p>
                  <p className="text-xs text-dark/50">
                    "{c.postTitle}" · {formatDate(c.createdAt)}
                  </p>
                </Link>
                <button
                  onClick={() => handleDeleteComment(c.id)}
                  className="shrink-0 rounded-md border border-red-300 px-2 py-1 text-xs text-red-500 hover:bg-red-50"
                >
                  삭제
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  );
}
