import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { signup } from '../api/auth';
import AuthField from '../components/AuthField';
import { getTourState, maybeStartTour } from '../store/tourStore';

export default function SignupPage() {
  const navigate = useNavigate();
  const location = useLocation();
  // 가입 후 자동 로그인 → 원래 경로가 있으면 복귀, 없으면 기본은 대시보드.
  const from = location.state?.from || '/dashboard';
  const [loginId, setLoginId] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      // 회원가입 성공 시 바로 로그인 상태가 된다.
      await signup({ loginId, password, nickname });
      // 신규 가입 → 온보딩 투어 시작(같은 브라우저에서 이미 본 계정이면 생략).
      // 투어가 시작되면 스스로 페이지를 이동시키고 최종적으로 /dashboard 에서 끝난다.
      maybeStartTour(loginId);
      if (!getTourState().active) {
        navigate(from, { replace: true }); // 투어가 안 뜨는 경우만 기본 이동
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-sm px-4 py-16">
      <form
        onSubmit={handleSubmit}
        className="space-y-4 rounded-2xl border border-slate-200 bg-white p-8 shadow-sm"
      >
        <h1 className="text-2xl font-bold text-primary">회원가입</h1>

        <AuthField label="아이디" value={loginId} onChange={setLoginId} autoComplete="username" />
        <AuthField label="닉네임" value={nickname} onChange={setNickname} autoComplete="nickname" />
        <AuthField
          label="비밀번호"
          type="password"
          value={password}
          onChange={setPassword}
          autoComplete="new-password"
        />

        {error && <p className="text-sm text-red-500">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-primary px-4 py-2 font-semibold text-white
                     transition hover:bg-light disabled:opacity-50"
        >
          {loading ? '가입 중...' : '회원가입'}
        </button>

        <p className="text-center text-sm text-dark/60">
          이미 계정이 있으신가요?{' '}
          <Link to="/login" state={{ from }} className="font-medium text-secondary hover:text-primary">
            로그인
          </Link>
        </p>
      </form>
    </div>
  );
}
