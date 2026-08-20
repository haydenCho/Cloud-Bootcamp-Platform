import { useState } from 'react';
import { Link } from 'react-router-dom';
import LearnSection from './LearnSection';
import BlankSection from './BlankSection';

/**
 * GENERAL 단원 상세 = "학습" / "빈칸 채우기" 두 탭.
 */
export default function GeneralUnitPage({ unit }) {
  const [tab, setTab] = useState('learn'); // learn | blank

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <div className="mb-2">
        <Link to="/" className="text-sm text-secondary hover:text-primary">
          ← 로드맵
        </Link>
      </div>
      <h1 className="mb-6 text-2xl font-extrabold text-dark">{unit.name}</h1>

      {/* 탭 */}
      <div className="mb-6 flex gap-1 border-b border-slate-200">
        {[
          { key: 'learn', label: '학습' },
          { key: 'blank', label: '빈칸 채우기' },
        ].map((t) => (
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

      {/* 탭 전환 시 언마운트되도록 조건부 렌더링 (스크롤 리스너 정리 목적) */}
      {tab === 'learn' && <LearnSection unit={unit} />}
      {tab === 'blank' && <BlankSection unit={unit} />}
    </div>
  );
}
