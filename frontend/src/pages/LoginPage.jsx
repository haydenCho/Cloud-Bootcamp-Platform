import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login } from '../api/auth';
import AuthField from '../components/AuthField';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  // LoginPrompt/RequireAuth 가 넘겨준 원래 경로가 있으면 그곳으로 복귀, 없으면 기본은 대시보드.
  const from = location.state?.from || '/dashboard';
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login({ loginId, password });
      navigate(from, { replace: true }); // 로그인 성공 → 원래 가려던 곳(또는 로드맵)으로
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
        <h1 className="text-2xl font-bold text-primary">로그인</h1>

        <AuthField label="아이디" value={loginId} onChange={setLoginId} autoComplete="username" />
        <AuthField
          label="비밀번호"
          type="password"
          value={password}
          onChange={setPassword}
          autoComplete="current-password"
        />

        {error && <p className="text-sm text-red-500">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-primary px-4 py-2 font-semibold text-white
                     transition hover:bg-light disabled:opacity-50"
        >
          {loading ? '로그인 중...' : '로그인'}
        </button>

        <p className="text-center text-sm text-dark/60">
          계정이 없으신가요?{' '}
          <Link to="/signup" state={{ from }} className="font-medium text-secondary hover:text-primary">
            회원가입
          </Link>
        </p>
      </form>
    </div>
  );
}
