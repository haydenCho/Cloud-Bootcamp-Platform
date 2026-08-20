import { useLocation, useNavigate } from 'react-router-dom';

/**
 * 인라인 액션(예: 좋아요 버튼)에서 비로그인 사용자를 유도하는 공통 모달.
 * 페이지 전체를 감싸는 RequireAuth/LoginPrompt 와 짝을 이루는, 액션용 재사용 컴포넌트다.
 * "로그인이 필요합니다" 안내 + 확인 시 현재 경로를 기억해 /login 으로 이동(로그인 후 복귀).
 */
export default function LoginRequiredModal({ open, onClose, description }) {
  const navigate = useNavigate();
  const location = useLocation();
  if (!open) return null;

  const from = location.pathname + location.search;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
      onClick={onClose}
    >
      <div
        className="w-full max-w-xs rounded-2xl bg-white p-6 text-center shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mx-auto mb-3 flex h-11 w-11 items-center justify-center rounded-full bg-secondary/15 text-xl">
          🔒
        </div>
        <h2 className="text-base font-bold text-dark">로그인이 필요합니다</h2>
        <p className="mt-1 text-sm text-dark/60">
          {description ?? '이 기능은 로그인 후 이용할 수 있습니다.'}
        </p>
        <div className="mt-5 flex gap-2">
          <button
            onClick={onClose}
            className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm text-dark/70 transition hover:border-primary hover:text-primary"
          >
            닫기
          </button>
          <button
            onClick={() => navigate('/login', { state: { from } })}
            className="flex-1 rounded-lg bg-primary px-3 py-2 text-sm font-semibold text-white transition hover:bg-light"
          >
            로그인하러 가기
          </button>
        </div>
      </div>
    </div>
  );
}
