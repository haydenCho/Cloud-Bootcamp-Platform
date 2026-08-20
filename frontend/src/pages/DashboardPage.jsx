import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../store/authStore';
import ProgressSection from '../components/dashboard/ProgressSection';
import GrassSection from '../components/dashboard/GrassSection';
import AccountSection from '../components/dashboard/AccountSection';
import MyActivitySection from '../components/dashboard/MyActivitySection';

const TABS = [
  { key: 'progress', label: '진도' },
  { key: 'grass', label: '잔디심기' },
  { key: 'activity', label: '내 커뮤니티 활동' },
  { key: 'account', label: '계정 관리' },
];

/**
 * 대시보드(마이페이지) 뼈대 — 3단계.
 * 진도/잔디심기는 더미 데이터, 계정 관리는 실제 API 연동.
 * 커뮤니티 글/댓글 관리 탭은 post/comment 도메인이 없어 이번 범위 밖(6단계).
 */
export default function DashboardPage() {
  const { user } = useAuth();
  const [tab, setTab] = useState('progress');

  // 대시보드는 개인 페이지이므로 로그인 필요
  if (!user) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-24 text-center">
        <h1 className="text-2xl font-bold text-dark">대시보드</h1>
        <p className="mt-3 text-dark/60">로그인 후 이용할 수 있습니다.</p>
        <Link
          to="/login"
          className="mt-8 inline-block rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-light"
        >
          로그인하기
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10">
      <h1 className="mb-6 text-3xl font-extrabold text-dark">
        {user.nickname} 님의 대시보드
      </h1>

      {/* 탭 */}
      <div className="mb-8 flex gap-1 border-b border-slate-200">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium transition-colors ${
              tab === t.key
                ? 'border-primary text-primary'
                : 'border-transparent text-dark/50 hover:text-primary'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'progress' && <ProgressSection />}
      {tab === 'grass' && <GrassSection />}
      {tab === 'activity' && <MyActivitySection />}
      {tab === 'account' && <AccountSection />}
    </div>
  );
}
