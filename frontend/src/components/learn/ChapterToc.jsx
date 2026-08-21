import { useState } from 'react';

/**
 * 재사용 목차 컴포넌트. 같은 목차 데이터와 클릭 동작(onSelect)을 두 UI 가 공유한다.
 *  (1) inline: 본문 상단의 토글형 목차(클릭으로 펼치기/접기)
 *  (2) floating: 화면 우측 고정 패널 — 평소 opacity 낮게, hover 시 진하게(transition)
 *
 * props:
 *  - items: [{ id, text, level }]  (level 2=대제목, 3=소제목)
 *  - activeId: 현재 보고 있는 섹션 id (강조)
 *  - onSelect(id): 항목 클릭 시 호출(스무스 스크롤은 부모가 담당)
 *  - variant: 'inline' | 'floating'
 */
export default function ChapterToc({ items, activeId, onSelect, variant = 'inline' }) {
  const [open, setOpen] = useState(false);

  if (!items || items.length === 0) return null;

  function handleClick(e, id) {
    e.preventDefault();
    onSelect(id);
    if (variant === 'inline') setOpen(false);
  }

  const list = (
    <ul className="space-y-1">
      {items.map((it) => (
        <li key={it.id} style={{ paddingLeft: `${(it.level - 2) * 12}px` }}>
          <a
            href={`#${it.id}`}
            onClick={(e) => handleClick(e, it.id)}
            className={`block truncate rounded px-2 py-1 text-sm transition-colors ${
              activeId === it.id
                ? 'bg-primary/10 font-semibold text-primary'
                : 'text-dark/60 hover:bg-slate-100 hover:text-primary'
            } ${it.level >= 3 ? 'text-[13px]' : ''}`}
            title={it.text}
          >
            {it.text}
          </a>
        </li>
      ))}
    </ul>
  );

  if (variant === 'floating') {
    // 우측 고정 패널: 평소 옅게(opacity-40), hover 시 진하게. lg 이상에서만 노출.
    return (
      <nav
        aria-label="목차"
        className="fixed right-4 top-1/2 z-20 hidden max-h-[70vh] w-60 -translate-y-1/2 overflow-y-auto
                   rounded-xl border border-slate-200 bg-white/95 p-3 opacity-40 shadow-lg
                   backdrop-blur transition-opacity duration-300 hover:opacity-100 xl:block"
      >
        <p className="mb-2 px-2 text-xs font-bold uppercase tracking-wide text-dark/40">목차</p>
        {list}
      </nav>
    );
  }

  // inline 토글형 목차
  return (
    <div className="mb-6 rounded-xl border border-slate-200 bg-slate-50/70">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="flex w-full items-center justify-between px-4 py-2.5 text-sm font-semibold text-dark"
        aria-expanded={open}
      >
        <span>📑 목차 ({items.length})</span>
        <span className={`transition-transform ${open ? 'rotate-180' : ''}`}>▾</span>
      </button>
      {open && <div className="border-t border-slate-200 px-3 py-2">{list}</div>}
    </div>
  );
}
