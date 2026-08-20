import { useEffect, useRef, useState } from 'react';
import DOMPurify from 'dompurify';
import { getContent } from '../../api/content';
import { getProgress, updateProgress } from '../../api/progress';
import { useAuth } from '../../store/authStore';

/**
 * 학습 섹션.
 * - content API 로 받은 HTML 을 DOMPurify 로 sanitize 후 렌더링.
 * - 페이지 스크롤 비율을 추적해 디바운스로 PATCH progress 호출.
 * - 90% 이상 스크롤 시 완료 처리(백엔드에서 completed=true) 및 완료 UI 표시.
 * - 비로그인 시에는 열람만 가능하고 진도는 저장하지 않는다.
 */
export default function LearnSection({ unit }) {
  const { user } = useAuth();
  const loggedIn = !!user;

  const [content, setContent] = useState(null);
  const [status, setStatus] = useState('loading'); // loading | done | empty | error
  const [percent, setPercent] = useState(0); // 도달한 최대 스크롤 %
  const [completed, setCompleted] = useState(false);

  const percentRef = useRef(0); // 최신 percent (리스너/타이머에서 참조)
  const savedRef = useRef(0); // 서버에 마지막으로 저장된 percent
  const saveTimer = useRef(null);

  // 1) 콘텐츠 로드 + (로그인 시) 기존 진도 복원
  useEffect(() => {
    let alive = true;
    setStatus('loading');
    getContent(unit.code)
      .then((data) => {
        if (!alive) return;
        setContent(data);
        setStatus('done');
      })
      .catch((err) => {
        if (!alive) return;
        setStatus(err.message?.includes('콘텐츠') ? 'empty' : 'error');
      });

    if (loggedIn) {
      getProgress()
        .then((list) => {
          if (!alive) return;
          const mine = list.find((p) => p.unitCode === unit.code);
          if (mine) {
            setPercent(mine.generalPercent);
            percentRef.current = mine.generalPercent;
            savedRef.current = mine.generalPercent;
            if (mine.generalPercent >= 90) setCompleted(true);
          }
        })
        .catch(() => {});
    }
    return () => {
      alive = false;
    };
  }, [unit.code, loggedIn]);

  // 2) 스크롤 추적 + 디바운스 저장
  useEffect(() => {
    if (status !== 'done') return undefined;

    function computePercent() {
      const doc = document.documentElement;
      const scrollable = doc.scrollHeight - window.innerHeight;
      if (scrollable <= 4) return 100; // 스크롤이 필요 없는 짧은 콘텐츠는 100%(모두 표시됨)
      const p = Math.round((window.scrollY / scrollable) * 100);
      return Math.min(100, Math.max(0, p));
    }

    function persist() {
      const p = percentRef.current;
      if (!loggedIn || p <= savedRef.current) return;
      savedRef.current = p;
      updateProgress(unit.code, p)
        .then((res) => {
          if (res?.completed) setCompleted(true);
        })
        .catch(() => {
          savedRef.current = 0; // 실패 시 다음 기회에 재시도되도록
        });
    }

    function onScroll() {
      const p = computePercent();
      if (p > percentRef.current) {
        percentRef.current = p;
        setPercent(p);
        if (p >= 90) setCompleted(true);
      }
      clearTimeout(saveTimer.current);
      saveTimer.current = setTimeout(persist, 600);
    }

    // 초기 1회 계산(짧은 콘텐츠 대응) + 리스너 등록
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => {
      window.removeEventListener('scroll', onScroll);
      clearTimeout(saveTimer.current);
      persist(); // 이탈 시 마지막 위치 저장
    };
  }, [status, unit.code, loggedIn]);

  if (status === 'loading') return <p className="py-16 text-center text-slate-400">불러오는 중...</p>;
  if (status === 'error') return <p className="py-16 text-center text-red-500">콘텐츠를 불러오지 못했습니다.</p>;
  if (status === 'empty')
    return <p className="py-16 text-center text-dark/50">아직 등록된 학습 콘텐츠가 없습니다.</p>;

  const safeHtml = DOMPurify.sanitize(content.body);

  return (
    <div>
      {/* 진도 바 */}
      <div className="sticky top-16 z-10 mb-6 bg-slate-50 pb-2 pt-1">
        <div className="mb-1 flex items-center justify-between text-xs">
          <span className="text-dark/60">
            학습 진도 <span className="font-semibold text-primary">{percent}%</span>
            {!loggedIn && <span className="ml-2 text-dark/40">(로그인하면 진도가 저장됩니다)</span>}
          </span>
          {completed && (
            <span className="rounded-full bg-accent/20 px-2 py-0.5 text-[11px] font-bold text-[#8a7400]">
              학습 완료 ✓
            </span>
          )}
        </div>
        <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-200">
          <div
            className={`h-full rounded-full transition-all ${completed ? 'bg-accent' : 'bg-primary'}`}
            style={{ width: `${percent}%` }}
          />
        </div>
      </div>

      {/* 본문 (sanitize 됨) */}
      <article
        className="learn-content leading-relaxed text-dark/90"
        dangerouslySetInnerHTML={{ __html: safeHtml }}
      />
    </div>
  );
}
