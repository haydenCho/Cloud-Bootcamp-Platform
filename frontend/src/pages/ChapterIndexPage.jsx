import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getUnits } from '../api/unit';
import { getChapters } from '../api/chapters';
import BlankSection from '../components/learn/BlankSection';

/**
 * /study/:unitCode — 단원 챕터 목록(블로그 인덱스).
 * 챕터 카드 클릭 시 /study/:unitCode/:sortOrder 로 이동.
 * GENERAL 단원의 빈칸 채우기는 단원 단위로 유지되므로 하단에 접이식 섹션으로 함께 제공한다.
 */
export default function ChapterIndexPage() {
  const { unitCode } = useParams();
  const [unit, setUnit] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | done | error
  const [showBlanks, setShowBlanks] = useState(false);

  useEffect(() => {
    let alive = true;
    setStatus('loading');
    Promise.all([getUnits(), getChapters(unitCode)])
      .then(([units, chs]) => {
        if (!alive) return;
        setUnit(units.find((u) => u.code === unitCode) ?? null);
        setChapters(chs);
        setStatus('done');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, [unitCode]);

  if (status === 'loading') {
    return <p className="py-24 text-center text-slate-400">불러오는 중...</p>;
  }
  if (status === 'error') {
    return <p className="py-24 text-center text-red-500">단원 정보를 불러오지 못했습니다.</p>;
  }

  const hasBlanks = unit && unit.blankCount > 0;

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <div className="mb-2">
        <Link to="/study" className="text-sm text-secondary hover:text-primary">
          ← 학습하기
        </Link>
      </div>
      <h1 className="mb-1 text-2xl font-extrabold text-dark">{unit?.name ?? unitCode}</h1>
      <p className="mb-6 text-sm text-dark/50">
        챕터 {chapters.length}개 · 원하는 챕터부터 읽어보세요
        {' '}
        <span className="text-dark/40">(로그인하면 읽은 챕터가 진도에 기록됩니다)</span>
      </p>

      {chapters.length === 0 ? (
        <p className="py-16 text-center text-dark/50">아직 등록된 학습 콘텐츠가 없습니다.</p>
      ) : (
        <ol className="space-y-3">
          {chapters.map((c) => (
            <li key={c.id}>
              <Link
                to={`/study/${unitCode}/${c.sortOrder}`}
                className="flex items-center gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm
                           transition hover:-translate-y-0.5 hover:border-primary hover:shadow-md"
              >
                <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
                  {c.sortOrder}
                </span>
                <span className="min-w-0 flex-1 truncate font-semibold text-dark">{c.title}</span>
                <span className="shrink-0 text-sm text-dark/30">→</span>
              </Link>
            </li>
          ))}
        </ol>
      )}

      {/* 빈칸 채우기 (단원 단위, 접이식) */}
      {hasBlanks && (
        <div className="mt-8 rounded-2xl border border-slate-200 bg-slate-50/70">
          <button
            type="button"
            onClick={() => setShowBlanks((v) => !v)}
            className="flex w-full items-center justify-between px-4 py-3 text-sm font-semibold text-dark"
            aria-expanded={showBlanks}
          >
            <span>✏️ 빈칸 채우기 ({unit.blankCount}문제)</span>
            <span className={`transition-transform ${showBlanks ? 'rotate-180' : ''}`}>▾</span>
          </button>
          {showBlanks && (
            <div className="border-t border-slate-200 px-4 py-4">
              <BlankSection unit={unit} />
            </div>
          )}
        </div>
      )}
    </div>
  );
}
