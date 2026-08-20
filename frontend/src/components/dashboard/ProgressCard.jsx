import { useState } from 'react';

/**
 * 대시보드 진도 카드 (단원 1개).
 * - 아이콘 투명도를 진도(overall)에 따라 다르게 준다.
 * - 완료(100%) 단원은 accent ring + glow 로 강조한다.
 * - GENERAL: 일반 학습 / 빈칸 채우기 두 지표, PRACTICE: 실습 완료율 한 지표.
 *
 * progress 는 mockProgress.getMockProgress() 가 준 해당 code 의 항목.
 * (4단계에서 실제 progress API 로 교체돼도 이 컴포넌트는 그대로 재사용된다.)
 */
export default function ProgressCard({ unit, progress }) {
  const [imgFailed, setImgFailed] = useState(false);

  const isPractice = unit.type === 'PRACTICE';
  const metrics = isPractice
    ? [{ label: '실습 완료율', value: progress.practicePercent }]
    : [
        { label: '일반 학습', value: progress.generalPercent },
        { label: '빈칸 채우기', value: progress.blankPercent },
      ];

  const overall = Math.round(
    metrics.reduce((sum, m) => sum + m.value, 0) / metrics.length,
  );
  const completed = metrics.every((m) => m.value === 100);

  // 진도에 따른 아이콘 투명도 (0% → 0.35, 100% → 1.0)
  const iconOpacity = 0.35 + 0.65 * (overall / 100);
  const ringColor = isPractice ? 'border-secondary' : 'border-primary';
  const initial = unit.name?.trim().charAt(0) ?? '?';

  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      {/* 아이콘 */}
      <span
        className={`flex h-16 w-16 items-center justify-center rounded-full border-2 bg-white ${
          completed
            ? 'border-accent shadow-[0_0_16px_rgba(236,200,21,0.65)] ring-2 ring-accent/60'
            : ringColor
        }`}
      >
        <span style={{ opacity: iconOpacity }} className="flex items-center justify-center">
          {unit.iconImagePath && !imgFailed ? (
            <img
              src={unit.iconImagePath}
              alt={unit.name}
              className="h-9 w-9 object-contain"
              onError={() => setImgFailed(true)}
            />
          ) : (
            <span className="text-xl font-bold text-primary">{initial}</span>
          )}
        </span>
      </span>

      {/* 이름 + 완료 뱃지 */}
      <div className="flex flex-col items-center gap-1">
        <span className="text-center text-sm font-semibold text-dark">{unit.name}</span>
        {completed && (
          <span className="rounded-full bg-accent/20 px-2 py-0.5 text-[11px] font-bold text-[#8a7400]">
            완료
          </span>
        )}
      </div>

      {/* 지표 바 */}
      <div className="w-full space-y-2">
        {metrics.map((m) => (
          <div key={m.label}>
            <div className="mb-0.5 flex items-center justify-between text-[11px] text-dark/60">
              <span>{m.label}</span>
              <span className="font-semibold text-dark/80">{m.value}%</span>
            </div>
            <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-100">
              <div
                className={`h-full rounded-full ${
                  m.value === 100 ? 'bg-accent' : isPractice ? 'bg-secondary' : 'bg-primary'
                }`}
                style={{ width: `${m.value}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
