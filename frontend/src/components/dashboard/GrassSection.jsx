import { useMemo } from 'react';
import { getMockActivity } from '../../mocks/mockActivity';

// level(0~4) → 팔레트 톤 차등 색상
const LEVEL_CLASS = [
  'bg-slate-200', // 0: 활동 없음
  'bg-light/50', // 1
  'bg-light', // 2
  'bg-secondary', // 3
  'bg-primary', // 4
];

const CELL = '0.75rem'; // 12px

/**
 * 잔디심기 섹션 (GitHub 스타일).
 * ⚠️ 활동량은 더미(getMockActivity). 4단계에서 실제 activity_log API 로 교체 예정.
 * 별도 라이브러리 없이 CSS Grid + 팔레트 톤 차등으로만 렌더링한다.
 */
export default function GrassSection() {
  const days = useMemo(() => getMockActivity(26), []); // 약 6개월
  const total = useMemo(() => days.reduce((sum, d) => sum + d.count, 0), [days]);

  return (
    <section>
      <div className="mb-4 flex items-baseline justify-between">
        <h2 className="text-xl font-bold text-dark">잔디심기</h2>
        <span className="text-xs text-dark/50">※ 활동량은 아직 더미 데이터입니다</span>
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
        <p className="mb-3 text-sm text-dark/70">
          최근 6개월 활동 <span className="font-semibold text-primary">{total}</span>회
        </p>

        <div className="overflow-x-auto">
          <div className="flex gap-2">
            {/* 요일 라벨 (월/수/금) */}
            <div
              className="grid gap-1 pr-1 text-[9px] leading-none text-dark/40"
              style={{ gridTemplateRows: `repeat(7, ${CELL})` }}
            >
              <span />
              <span>월</span>
              <span />
              <span>수</span>
              <span />
              <span>금</span>
              <span />
            </div>

            {/* 주(열) × 요일(행) 그리드 */}
            <div
              className="grid gap-1"
              style={{
                gridTemplateRows: `repeat(7, ${CELL})`,
                gridAutoFlow: 'column',
                gridAutoColumns: CELL,
              }}
            >
              {days.map((d) => (
                <span
                  key={d.date}
                  className={`rounded-sm ${LEVEL_CLASS[d.level]}`}
                  title={`${d.date} · 활동 ${d.count}회`}
                />
              ))}
            </div>
          </div>
        </div>

        {/* 범례 */}
        <div className="mt-3 flex items-center justify-end gap-1 text-[11px] text-dark/50">
          <span className="mr-1">적음</span>
          {LEVEL_CLASS.map((cls, i) => (
            <span key={i} className={`h-3 w-3 rounded-sm ${cls}`} />
          ))}
          <span className="ml-1">많음</span>
        </div>
      </div>
    </section>
  );
}
