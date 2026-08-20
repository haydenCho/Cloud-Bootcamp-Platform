import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../store/authStore';
import { goToStep, markTourSeen, stopTour, useTour } from '../../store/tourStore';

/**
 * 온보딩 투어. 회원가입 직후에만 뜬다(tourStore.startTour).
 * 구현 방식: 헤더 요소를 실제 하이라이트 + 각 단계마다 해당 페이지로 이동(자연스러움) —
 * 헤더는 모든 라우트에서 항상 마운트돼 있어 하이라이트 앵커가 안정적이라 페이지 이동 방식을 택했다.
 * 스포트라이트는 대상 위 요소에 거대한 box-shadow(spread)로 주변만 어둡게 하는 순수 CSS 기법(라이브러리 없음).
 */

// step 0 = 환영 카드(중앙), step 1~4 = 헤더 메뉴 하이라이트
const STEPS = [
  { welcome: true, path: '/dashboard' },
  {
    target: '[data-tour="roadmap"]',
    path: '/',
    title: '로드맵',
    body: '학습 단원을 한눈에 확인하는 곳이에요. 노드를 클릭하면 학습이 시작됩니다.',
  },
  {
    target: '[data-tour="study"]',
    path: '/study',
    title: '학습하기',
    body: '실제 학습 콘텐츠 목록을 보는 곳이에요. 단원별 콘텐츠와 빈칸 문제를 확인할 수 있어요.',
  },
  {
    target: '[data-tour="dashboard"]',
    path: '/dashboard',
    title: '대시보드',
    body: '학습 진도와 잔디심기(활동 기록)를 확인하는 곳이에요.',
  },
  {
    target: '[data-tour="community"]',
    path: '/community',
    title: '커뮤니티',
    body: '질문과 답변을 나누는 곳이에요. 궁금한 걸 자유롭게 물어보세요.',
  },
];

const LAST = STEPS.length - 1;
const HIGHLIGHT_STEPS = STEPS.length - 1; // 환영 카드 제외한 안내 단계 수

export default function OnboardingTour() {
  const { active, step } = useTour();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [rect, setRect] = useState(null);

  // 단계 변경 시: 해당 페이지로 이동 + 대상 요소 위치 측정
  useEffect(() => {
    if (!active) return undefined;
    const s = STEPS[step];
    navigate(s.path);

    if (s.welcome) {
      setRect(null);
      return undefined;
    }
    const measure = () => {
      const el = document.querySelector(s.target);
      const r = el ? el.getBoundingClientRect() : null;
      // 폭 0(예: 좁은 화면에서 헤더 nav 숨김)이면 앵커 없이 중앙 툴팁으로 처리
      setRect(r && r.width > 0 ? r : null);
    };
    const raf = requestAnimationFrame(measure);
    window.addEventListener('resize', measure);
    return () => {
      cancelAnimationFrame(raf);
      window.removeEventListener('resize', measure);
    };
  }, [active, step, navigate]);

  if (!active) return null;

  const current = STEPS[step];

  function finish() {
    markTourSeen(user?.loginId);
    stopTour();
    navigate('/dashboard'); // 투어 종료/건너뛰기 후 최종 위치
  }
  function next() {
    if (step >= LAST) finish();
    else goToStep(step + 1);
  }

  // 환영 카드 (중앙)
  if (current.welcome) {
    return (
      <div className="fixed inset-0 z-[100] flex items-center justify-center bg-dark/55 px-4">
        <div className="w-full max-w-sm rounded-2xl bg-white p-8 text-center shadow-2xl">
          <div className="mb-3 text-4xl">🎉</div>
          <h2 className="text-xl font-extrabold text-dark">환영합니다!</h2>
          <p className="mt-2 text-dark/60">먼저 학습 방법을 알아볼까요?</p>
          <div className="mt-6 flex gap-2">
            <button
              onClick={finish}
              className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm text-dark/70 transition hover:border-primary hover:text-primary"
            >
              건너뛰기
            </button>
            <button
              onClick={next}
              className="flex-1 rounded-lg bg-primary px-3 py-2 text-sm font-semibold text-white transition hover:bg-light"
            >
              시작하기
            </button>
          </div>
        </div>
      </div>
    );
  }

  // 안내 단계 (헤더 하이라이트 + 툴팁)
  const tipStyle = rect
    ? {
        position: 'fixed',
        zIndex: 102,
        top: rect.bottom + 14,
        left: Math.max(12, Math.min(rect.left, (typeof window !== 'undefined' ? window.innerWidth : 400) - 320)),
      }
    : { position: 'fixed', zIndex: 102, top: '50%', left: '50%', transform: 'translate(-50%, -50%)' };

  return (
    <>
      {/* 클릭 차단 레이어. rect 가 없으면 여기서 배경을 어둡게 */}
      <div
        className="fixed inset-0 z-[100]"
        style={{ background: rect ? 'transparent' : 'rgba(22,35,38,0.55)' }}
      />

      {/* 스포트라이트: 대상만 밝게(거대한 box-shadow 로 주변을 어둡게) + accent 링 */}
      {rect && (
        <div
          style={{
            position: 'fixed',
            zIndex: 101,
            top: rect.top - 6,
            left: rect.left - 6,
            width: rect.width + 12,
            height: rect.height + 12,
            borderRadius: 10,
            border: '2px solid #ECC815',
            boxShadow: '0 0 0 9999px rgba(22,35,38,0.55)',
            pointerEvents: 'none',
          }}
        />
      )}

      {/* 툴팁 카드 */}
      <div style={tipStyle} className="w-[min(20rem,90vw)] rounded-2xl bg-white p-5 shadow-2xl">
        <div className="mb-1 text-xs font-semibold text-secondary">
          {step} / {HIGHLIGHT_STEPS}
        </div>
        <h3 className="text-base font-bold text-dark">{current.title}</h3>
        <p className="mt-1 text-sm text-dark/60">{current.body}</p>
        <div className="mt-4 flex items-center justify-between">
          <button onClick={finish} className="text-sm text-dark/40 hover:text-dark/70">
            건너뛰기
          </button>
          <button
            onClick={next}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-light"
          >
            {step >= LAST ? '완료' : '다음'}
          </button>
        </div>
      </div>
    </>
  );
}
