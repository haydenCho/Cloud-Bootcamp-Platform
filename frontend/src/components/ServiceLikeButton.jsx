import { useEffect, useState } from 'react';
import { useAuth } from '../store/authStore';
import { getServiceLike, toggleServiceLike } from '../api/serviceLike';
import LoginRequiredModal from './auth/LoginRequiredModal';

/**
 * 서비스 좋아요 버튼 (헤더에 상주 — 모든 페이지에서 노출).
 * - 총 카운트 + 내가 눌렀는지 표시.
 * - 로그인 사용자: 클릭 시 토글(서버 응답으로 상태 갱신, 새로고침해도 유지).
 * - 비로그인 사용자: LoginRequiredModal 로 로그인 유도(CLAUDE.md 규칙).
 */
export default function ServiceLikeButton() {
  const { user } = useAuth();
  const [count, setCount] = useState(0);
  const [liked, setLiked] = useState(false);
  const [busy, setBusy] = useState(false);
  const [promptOpen, setPromptOpen] = useState(false);

  // 마운트 및 로그인 상태 변화 시 상태 로드(로그인/로그아웃 후 likedByMe 갱신)
  useEffect(() => {
    let alive = true;
    getServiceLike()
      .then((data) => {
        if (!alive) return;
        setCount(data.totalCount);
        setLiked(data.likedByMe);
      })
      .catch(() => {});
    return () => {
      alive = false;
    };
  }, [user]);

  async function handleClick() {
    if (!user) {
      setPromptOpen(true);
      return;
    }
    if (busy) return;
    setBusy(true);
    try {
      const data = await toggleServiceLike();
      setCount(data.totalCount);
      setLiked(data.likedByMe);
    } catch (_) {
      // 무시(네트워크 오류 등)
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <button
        onClick={handleClick}
        disabled={busy}
        title="이 서비스가 마음에 든다면 좋아요!"
        className={`flex items-center gap-1 rounded-full border px-2.5 py-1 text-sm transition ${
          liked
            ? 'border-accent bg-accent/15 text-[#8a7400]'
            : 'border-slate-300 text-dark/60 hover:border-primary hover:text-primary'
        }`}
      >
        <span className={liked ? '' : 'grayscale'}>👍</span>
        <span className="font-semibold">{count}</span>
      </button>

      <LoginRequiredModal
        open={promptOpen}
        onClose={() => setPromptOpen(false)}
        description="좋아요는 로그인 후 누를 수 있습니다."
      />
    </>
  );
}
