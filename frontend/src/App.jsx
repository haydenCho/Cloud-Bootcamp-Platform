import { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import { bootstrapAuth } from './api/auth';
import Header from './components/Header';
import OnboardingTour from './components/onboarding/OnboardingTour';
import RequireAuth from './components/auth/RequireAuth';
import RoadmapPage from './pages/RoadmapPage';
import StudyPage from './pages/StudyPage';
import DashboardPage from './pages/DashboardPage';
import UnitDetailPage from './pages/UnitDetailPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import CommunityListPage from './pages/community/CommunityListPage';
import PostFormPage from './pages/community/PostFormPage';
import PostDetailPage from './pages/community/PostDetailPage';

/**
 * 앱 레이아웃: 상단 헤더 + 라우팅.
 * 커뮤니티는 열람·작성 모두 로그인 필요 → RequireAuth 로 감싼다(비로그인 시 LoginPrompt).
 */
export default function App() {
  const [ready, setReady] = useState(false);

  // 새로고침 시 refresh 쿠키로 세션 복구 시도 (성공/실패와 무관하게 화면은 렌더링)
  useEffect(() => {
    bootstrapAuth().finally(() => setReady(true));
  }, []);

  return (
    <div className="min-h-screen">
      <Header />
      <OnboardingTour />
      <main>
        {ready ? (
          <Routes>
            <Route path="/" element={<RoadmapPage />} />
            <Route path="/study" element={<StudyPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/units/:code" element={<UnitDetailPage />} />

            {/* 커뮤니티 — 전부 인증 필요 */}
            <Route
              path="/community"
              element={
                <RequireAuth description="커뮤니티는 로그인 후 이용할 수 있습니다.">
                  <CommunityListPage />
                </RequireAuth>
              }
            />
            <Route
              path="/community/new"
              element={
                <RequireAuth description="커뮤니티는 로그인 후 이용할 수 있습니다.">
                  <PostFormPage />
                </RequireAuth>
              }
            />
            <Route
              path="/community/:id"
              element={
                <RequireAuth description="커뮤니티는 로그인 후 이용할 수 있습니다.">
                  <PostDetailPage />
                </RequireAuth>
              }
            />
            <Route
              path="/community/:id/edit"
              element={
                <RequireAuth description="커뮤니티는 로그인 후 이용할 수 있습니다.">
                  <PostFormPage />
                </RequireAuth>
              }
            />

            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
          </Routes>
        ) : (
          <div className="flex min-h-[60vh] items-center justify-center text-slate-400">
            불러오는 중...
          </div>
        )}
      </main>
    </div>
  );
}
