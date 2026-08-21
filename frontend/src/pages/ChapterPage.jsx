import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getChapter, visitChapter } from '../api/chapters';
import { useAuth } from '../store/authStore';
import NoteArticle from '../components/learn/NoteArticle';
import ChapterToc from '../components/learn/ChapterToc';

const HEADER_OFFSET = 80; // sticky 헤더 높이 보정

/** 텍스트 기반 슬러그(한글 유지). 빈 값이면 'section'. */
function slugify(text) {
  const s = text
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/[^\w가-힣-]/g, '');
  return s || 'section';
}

/**
 * 본문 HTML 의 h2/h3 에 id 를 부여(없을 때만)하고 목차 데이터를 만든다.
 * id 를 DOM 이 아니라 HTML 문자열에 직접 심어, 리렌더로 사라지지 않게 한다(DOMPurify 는 id 유지).
 * @returns { html, toc: [{id, text, level}] }
 */
function buildToc(html) {
  if (!html) return { html: '', toc: [] };
  const toc = [];
  const used = new Set();
  const out = html.replace(/<h([23])([^>]*)>([\s\S]*?)<\/h\1>/gi, (m, lvl, attrs, inner) => {
    const text = inner.replace(/<[^>]+>/g, '').trim();
    let id = (attrs.match(/id="([^"]*)"/i) || [])[1];
    let newAttrs = attrs;
    if (!id) {
      const base = slugify(text);
      id = base;
      let n = 2;
      while (used.has(id)) id = `${base}-${n++}`;
      newAttrs = `${attrs} id="${id}"`;
    }
    used.add(id);
    toc.push({ id, text, level: Number(lvl) });
    return `<h${lvl}${newAttrs}>${inner}</h${lvl}>`;
  });
  return { html: out, toc };
}

export default function ChapterPage() {
  const { unitCode, sortOrder } = useParams();
  const order = Number(sortOrder);
  const { user } = useAuth();
  const loggedIn = !!user;

  const [chapter, setChapter] = useState(null);
  const [status, setStatus] = useState('loading'); // loading | done | error
  const [activeId, setActiveId] = useState(null);
  const containerRef = useRef(null);

  // 1) 챕터 로드
  useEffect(() => {
    let alive = true;
    setStatus('loading');
    setActiveId(null);
    window.scrollTo(0, 0);
    getChapter(unitCode, order)
      .then((data) => {
        if (!alive) return;
        setChapter(data);
        setStatus('done');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, [unitCode, order]);

  // 2) 방문 기록(로그인 시)
  useEffect(() => {
    if (status === 'done' && chapter && loggedIn) {
      visitChapter(chapter.id).catch(() => {});
    }
  }, [status, chapter, loggedIn]);

  // id 를 심은 본문 + 목차 데이터 (메모이즈 → html 참조가 안정적이라 article DOM 이 재생성되지 않음)
  const { html: processedHtml, toc } = useMemo(
    () => buildToc(chapter?.body),
    [chapter?.id, chapter?.body],
  );

  // 3) 렌더 후 현재 섹션 강조.
  //    IntersectionObserver 를 "헤딩이 상단 밴드를 지났다"는 트리거로 쓰고, 그때 실제 위치로
  //    현재 섹션을 결정론적으로 계산한다(점프 스크롤에도 정확). 초기 계산은 레이아웃이 끝난 뒤
  //    실행되도록 rAF 로 미룬다(효과 시점엔 getBoundingClientRect 가 아직 안정적이지 않을 수 있음).
  useEffect(() => {
    if (status !== 'done' || !containerRef.current || toc.length === 0) return undefined;
    const headings = Array.from(containerRef.current.querySelectorAll('h2[id], h3[id]'));
    if (headings.length === 0) return undefined;
    headings.forEach((h) => {
      h.style.scrollMarginTop = `${HEADER_OFFSET}px`;
    });

    let raf = 0;
    const recompute = () => {
      // 방어적으로 매번 현재 DOM 에서 헤딩을 다시 찾는다(리렌더로 노드가 교체돼도 안전).
      const live = Array.from(containerRef.current?.querySelectorAll('h2[id], h3[id]') || []);
      if (live.length === 0) return;
      const line = HEADER_OFFSET + 4;
      let current = live[0].id;
      for (const h of live) {
        if (h.getBoundingClientRect().top - line <= 0) current = h.id;
        else break;
      }
      setActiveId(current);
    };
    const schedule = () => {
      cancelAnimationFrame(raf);
      raf = requestAnimationFrame(recompute);
    };

    const observer = new IntersectionObserver(schedule, {
      rootMargin: `-${HEADER_OFFSET}px 0px -55% 0px`,
      threshold: 0,
    });
    headings.forEach((h) => observer.observe(h));
    window.addEventListener('scroll', schedule, { passive: true });
    schedule(); // 초기 활성 섹션(레이아웃 이후)

    return () => {
      observer.disconnect();
      window.removeEventListener('scroll', schedule);
      cancelAnimationFrame(raf);
    };
  }, [status, processedHtml, toc.length]);

  function handleSelect(id) {
    const el = document.getElementById(id);
    if (!el) return;
    const y = el.getBoundingClientRect().top + window.scrollY - HEADER_OFFSET;
    window.scrollTo({ top: y, behavior: 'smooth' });
    setActiveId(id);
  }

  if (status === 'loading') {
    return <p className="py-24 text-center text-slate-400">불러오는 중...</p>;
  }
  if (status === 'error') {
    return (
      <div className="mx-auto max-w-3xl px-4 py-24 text-center">
        <p className="text-red-500">챕터를 불러오지 못했습니다.</p>
        <Link to={`/study/${unitCode}`} className="mt-4 inline-block text-sm text-secondary hover:text-primary">
          ← 챕터 목록
        </Link>
      </div>
    );
  }

  const { prev, next } = chapter;

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      {/* 상단 네비 */}
      <div className="mb-2 flex items-center justify-between text-sm">
        <Link to={`/study/${unitCode}`} className="text-secondary hover:text-primary">
          ← 챕터 목록
        </Link>
        <span className="text-dark/40">챕터 {chapter.sortOrder}</span>
      </div>

      {!loggedIn && (
        <p className="mb-4 rounded-lg bg-slate-50 px-3 py-2 text-xs text-dark/50">
          로그인하면 읽은 챕터가 진도에 기록됩니다.
        </p>
      )}

      {/* 우측 고정 목차(평소 옅게 / hover 진하게) */}
      <ChapterToc items={toc} activeId={activeId} onSelect={handleSelect} variant="floating" />

      {/* 상단 토글형 목차 */}
      <ChapterToc items={toc} activeId={activeId} onSelect={handleSelect} variant="inline" />

      {/* 본문 */}
      <div ref={containerRef}>
        <NoteArticle html={processedHtml} />
      </div>

      {/* 이전/다음 네비게이션 */}
      <div className="mt-12 flex items-stretch justify-between gap-3 border-t border-slate-200 pt-6">
        {prev ? (
          <Link
            to={`/study/${unitCode}/${prev.sortOrder}`}
            className="group flex max-w-[48%] flex-col rounded-xl border border-slate-200 px-4 py-3 transition hover:border-primary hover:shadow-sm"
          >
            <span className="text-xs text-dark/40">← 이전</span>
            <span className="mt-0.5 truncate text-sm font-semibold text-dark group-hover:text-primary">
              {prev.title}
            </span>
          </Link>
        ) : (
          <span />
        )}
        {next ? (
          <Link
            to={`/study/${unitCode}/${next.sortOrder}`}
            className="group flex max-w-[48%] flex-col items-end rounded-xl border border-slate-200 px-4 py-3 text-right transition hover:border-primary hover:shadow-sm"
          >
            <span className="text-xs text-dark/40">다음 →</span>
            <span className="mt-0.5 truncate text-sm font-semibold text-dark group-hover:text-primary">
              {next.title}
            </span>
          </Link>
        ) : (
          <span />
        )}
      </div>
    </div>
  );
}
