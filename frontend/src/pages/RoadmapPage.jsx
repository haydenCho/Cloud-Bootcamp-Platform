import { useEffect, useRef, useState } from 'react';
import { getUnits } from '../api/unit';
import RoadmapNode from '../components/RoadmapNode';

const COLS = 6; // 한 줄에 놓을 단원 수 (가로 방향 스네이크 레이아웃)

// 진입 애니메이션 타이밍
const STEP = 70; // 노드 간 등장 간격(ms)
const LINE_OFFSET = 200; // 노드 등장 후 연결선이 그어지는 지연(ms)
const ENTRY_TAIL = 500; // 마지막 요소 애니메이션 여유(ms)

// 마우스 확대(dock) 파라미터
const RADIUS = 140; // 확대 영향 반경(px)
const AMP = 0.5; // 최대 추가 배율(→ 최대 scale 1.5)

function chunk(arr, size) {
  const rows = [];
  for (let i = 0; i < arr.length; i += size) rows.push(arr.slice(i, i + size));
  return rows;
}

/**
 * 메인홈 = 학습 로드맵.
 * - 진입 시 단원이 sort_order 순으로 fade + scale-in 되고, 뒤이어 연결선이 그어진다(순수 CSS 키프레임).
 * - 진입 애니메이션이 끝나면 마우스 확대(dock) 효과 활성화: 커서에 가까운 노드일수록 scale 로만 커진다.
 * - prefers-reduced-motion: reduce 면 진입 애니메이션(CSS)과 마우스 확대(JS) 모두 비활성 → 즉시 최종 상태.
 * - transform/opacity 만 사용해 레이아웃/스크롤에 영향을 주지 않는다.
 */
export default function RoadmapPage() {
  const [units, setUnits] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | done | error

  // 마우스 확대용 (React 리렌더 없이 ref + rAF 로 DOM transform 직접 조작)
  const nodeRefs = useRef([]); // 각 노드의 magnify 레이어 div
  const centers = useRef([]); // 각 노드 중심 좌표(scale=1 기준)
  const mouse = useRef({ x: 0, y: 0 });
  const rafId = useRef(0);
  const ready = useRef(false); // 진입 애니메이션 종료 후 true
  const reduced = useRef(false);

  useEffect(() => {
    let alive = true;
    getUnits()
      .then((data) => {
        if (!alive) return;
        setUnits(data);
        setStatus('done');
      })
      .catch(() => {
        if (alive) setStatus('error');
      });
    return () => {
      alive = false;
    };
  }, []);

  // 진입 애니메이션 종료 시점에 마우스 확대 활성화 (reduce 면 확대 자체를 끔)
  useEffect(() => {
    if (status !== 'done') return undefined;
    reduced.current =
      typeof window !== 'undefined' &&
      window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
    if (reduced.current) {
      ready.current = true; // 확대는 안 하지만 상태만 정리
      return undefined;
    }
    ready.current = false;
    const total = units.length * STEP + LINE_OFFSET + ENTRY_TAIL;
    const t = setTimeout(() => {
      ready.current = true;
    }, total);
    return () => clearTimeout(t);
  }, [status, units.length]);

  function measureCenters() {
    centers.current = nodeRefs.current.map((el) => {
      if (!el) return null;
      const r = el.getBoundingClientRect(); // scale=1 상태에서 측정
      return { x: r.left + r.width / 2, y: r.top + r.height / 2 };
    });
  }

  function applyMagnify() {
    rafId.current = 0;
    const { x, y } = mouse.current;
    nodeRefs.current.forEach((el, idx) => {
      if (!el) return;
      const c = centers.current[idx];
      if (!c) return;
      const d = Math.hypot(x - c.x, y - c.y);
      const f = Math.max(0, 1 - d / RADIUS);
      const s = 1 + AMP * f * f; // 가까울수록 부드럽게 커짐
      if (s > 1.001) {
        el.style.transform = `scale(${s})`;
        el.parentElement.style.zIndex = '10'; // 커진 노드를 위로
      } else {
        el.style.transform = '';
        el.parentElement.style.zIndex = '';
      }
    });
  }

  function handleMouseEnter() {
    if (reduced.current || !ready.current) return;
    measureCenters();
  }
  function handleMouseMove(e) {
    if (reduced.current || !ready.current) return;
    mouse.current = { x: e.clientX, y: e.clientY };
    if (!rafId.current) rafId.current = requestAnimationFrame(applyMagnify);
  }
  function handleMouseLeave() {
    if (rafId.current) {
      cancelAnimationFrame(rafId.current);
      rafId.current = 0;
    }
    nodeRefs.current.forEach((el) => {
      if (!el) return;
      el.style.transform = '';
      el.parentElement.style.zIndex = '';
    });
  }

  const rows = chunk(units, COLS);

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center px-4 py-6">
      {status === 'loading' && <p className="text-slate-400">로드맵을 불러오는 중...</p>}
      {status === 'error' && (
        <p className="text-red-500">로드맵을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.</p>
      )}

      {status === 'done' && (
        <div
          className="max-w-full overflow-x-auto"
          onMouseEnter={handleMouseEnter}
          onMouseMove={handleMouseMove}
          onMouseLeave={handleMouseLeave}
        >
          {/*
           * overflow-x-auto 는 overflow-y 도 auto 로 만들어 세로로 잘린다.
           * 웨이브 오프셋(±12px) + 강조 링/그림자 + 마우스 확대(최대 1.5배, 노드 높이의 ~1/4 만큼 상하로 커짐)
           * 가 잘리지 않도록 콘텐츠에 넉넉한 여백(py-16=64px, px-10=40px)을 미리 확보한다(스크롤은 새로 생기지 않음).
           */}
          <div className="mx-auto flex w-max flex-col items-center gap-10 px-10 py-16">
            {rows.map((row, r) => (
              <div
                key={r}
                className={`flex items-start ${r % 2 === 1 ? 'flex-row-reverse' : ''}`}
              >
                {row.map((unit, i) => {
                  const globalIndex = r * COLS + i;
                  // 완전한 일직선 대신 부드러운 파동으로 높이를 약간씩 다르게(자연스러운 느낌)
                  const offsetY = Math.round(12 * Math.sin(globalIndex * 0.8));
                  const isLast = i === row.length - 1;
                  return (
                    <div
                      key={unit.id}
                      className="flex items-start"
                      style={{ transform: `translateY(${offsetY}px)` }}
                    >
                      {/* magnify 레이어: 여기 transform(scale)만 ref 로 직접 조작 */}
                      <div
                        ref={(el) => (nodeRefs.current[globalIndex] = el)}
                        className="transition-transform duration-150 ease-out will-change-transform"
                      >
                        <RoadmapNode
                          unit={unit}
                          className="rm-node-in"
                          style={{ animationDelay: `${globalIndex * STEP}ms` }}
                        />
                      </div>
                      {!isLast && (
                        // 단원 사이 가로 연결선 (아이콘 원 중심 높이에 맞춤) — 그어지듯 등장
                        <span
                          className="rm-line-in mt-10 h-0.5 w-6 shrink-0 bg-secondary/40 sm:w-10"
                          style={{ animationDelay: `${globalIndex * STEP + LINE_OFFSET}ms` }}
                        />
                      )}
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
