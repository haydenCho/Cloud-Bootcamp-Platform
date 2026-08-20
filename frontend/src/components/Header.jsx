import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../store/authStore';
import { logout } from '../api/auth';
import ServiceLikeButton from './ServiceLikeButton';

/**
 * 상단 헤더.
 * 왼쪽: 서비스명 + 네비게이션(로드맵 / 대시보드 / 학습하기 / 커뮤니티).
 * 오른쪽: 비로그인 → 로그인/회원가입, 로그인 → (닉네임 +) 로그아웃.
 */
export default function Header() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const navLinkClass = ({ isActive }) =>
    `px-3 py-2 text-sm font-medium transition-colors ${
      isActive ? 'text-primary' : 'text-dark/70 hover:text-primary'
    }`;

  async function handleLogout() {
    await logout();
    navigate('/');
  }

  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
        {/* 왼쪽: 서비스명 + 네비게이션 */}
        <div className="flex items-center gap-2">
          <Link to="/" className="mr-2 text-lg font-extrabold text-primary">
            클라우드 부트캠프
          </Link>
          <nav className="hidden items-center sm:flex">
            <NavLink to="/" end className={navLinkClass}>
              로드맵
            </NavLink>
            <NavLink to="/dashboard" className={navLinkClass}>
              대시보드
            </NavLink>
            <NavLink to="/study" className={navLinkClass}>
              학습하기
            </NavLink>
            <NavLink to="/community" className={navLinkClass}>
              커뮤니티
            </NavLink>
          </nav>
        </div>

        {/* 오른쪽: 서비스 좋아요 + 인증 상태별 버튼 */}
        <div className="flex items-center gap-2">
          <ServiceLikeButton />
          {user ? (
            <>
              <span className="hidden text-sm text-dark/70 sm:inline">
                <span className="font-semibold text-primary">{user.nickname}</span> 님
              </span>
              <button
                onClick={handleLogout}
                className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-dark/80 transition hover:border-primary hover:text-primary"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="rounded-lg px-3 py-1.5 text-sm font-medium text-dark/80 transition hover:text-primary"
              >
                로그인
              </Link>
              <Link
                to="/signup"
                className="rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-light"
              >
                회원가입
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
