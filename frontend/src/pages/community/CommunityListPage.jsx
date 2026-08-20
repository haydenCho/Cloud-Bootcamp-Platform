import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getPosts } from '../../api/community';
import { formatDate } from '../../components/community/format';

/** 커뮤니티 게시글 목록. */
export default function CommunityListPage() {
  const [posts, setPosts] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | done | error

  useEffect(() => {
    let alive = true;
    getPosts()
      .then((data) => {
        if (!alive) return;
        setPosts(data);
        setStatus('done');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, []);

  return (
    <div className="mx-auto max-w-3xl px-4 py-10">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-3xl font-extrabold text-dark">커뮤니티</h1>
        <Link
          to="/community/new"
          className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-light"
        >
          글쓰기
        </Link>
      </div>

      {status === 'loading' && <p className="py-16 text-center text-slate-400">불러오는 중...</p>}
      {status === 'error' && (
        <p className="py-16 text-center text-red-500">목록을 불러오지 못했습니다.</p>
      )}

      {status === 'done' &&
        (posts.length === 0 ? (
          <p className="py-16 text-center text-dark/50">아직 게시글이 없습니다. 첫 글을 작성해보세요!</p>
        ) : (
          <ul className="divide-y divide-slate-200 rounded-2xl border border-slate-200 bg-white shadow-sm">
            {posts.map((p) => (
              <li key={p.id}>
                <Link to={`/community/${p.id}`} className="block px-5 py-4 transition hover:bg-slate-50">
                  <div className="flex items-start justify-between gap-3">
                    <h2 className="font-semibold text-dark">
                      {p.title}
                      {p.commentCount > 0 && (
                        <span className="ml-2 text-sm font-normal text-secondary">
                          [{p.commentCount}]
                        </span>
                      )}
                    </h2>
                    <span className="shrink-0 text-xs text-dark/40">조회 {p.viewCount}</span>
                  </div>
                  <div className="mt-1 text-xs text-dark/50">
                    {p.authorNickname} · {formatDate(p.createdAt)}
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        ))}
    </div>
  );
}
