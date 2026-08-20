import { useLocation, useNavigate } from 'react-router-dom';

/**
 * 공통 "로그인이 필요합니다" 안내 컴포넌트 (CLAUDE.md "로그인 필요 안내 UX" 규칙).
 * 확인을 누르면 현재 경로를 기억해 /login 으로 이동하고, 로그인 성공 후 원래 경로로 복귀한다.
 * 로그인 유도가 필요한 어떤 화면/액션에서도 재사용한다.
 */
export default function LoginPrompt({ message = '로그인이 필요합니다.', description }) {
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.pathname + location.search;

  return (
    <div className="mx-auto max-w-md px-4 py-24 text-center">
      <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-secondary/15 text-2xl">
          🔒
        </div>
        <h1 className="text-xl font-bold text-dark">{message}</h1>
        <p className="mt-2 text-sm text-dark/60">
          {description ?? '이 기능은 로그인 후 이용할 수 있습니다.'}
        </p>
        <button
          onClick={() => navigate('/login', { state: { from } })}
          className="mt-6 w-full rounded-lg bg-primary px-4 py-2 font-semibold text-white transition hover:bg-light"
        >
          로그인하러 가기
        </button>
      </div>
    </div>
  );
}
