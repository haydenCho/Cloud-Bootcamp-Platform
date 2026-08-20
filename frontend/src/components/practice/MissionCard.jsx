/**
 * 실습 미션 카드. 완료 시 accent 톤으로 구분(4단계 완료 UI 와 통일감).
 */
export default function MissionCard({ mission, index, onSelect }) {
  const { title, description, xpReward, completed } = mission;
  return (
    <button
      type="button"
      onClick={() => onSelect(mission)}
      className={`flex flex-col gap-2 rounded-2xl border bg-white p-5 text-left shadow-sm transition
                  hover:-translate-y-0.5 hover:shadow-md ${
                    completed
                      ? 'border-accent ring-1 ring-accent/50'
                      : 'border-slate-200 hover:border-primary'
                  }`}
    >
      <div className="flex items-start justify-between gap-2">
        <span className="text-xs font-semibold text-primary">MISSION {index + 1}</span>
        {completed ? (
          <span className="rounded-full bg-accent/20 px-2 py-0.5 text-[11px] font-bold text-[#8a7400]">
            완료 ✓
          </span>
        ) : (
          <span className="rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-dark/50">
            미완료
          </span>
        )}
      </div>

      <h3 className="text-base font-bold text-dark">{title}</h3>
      <p className="line-clamp-2 text-sm text-dark/60">{description}</p>

      <div className="mt-1 flex items-center justify-between">
        <span className="text-xs font-semibold text-secondary">+{xpReward} XP</span>
        <span className="text-xs text-primary">실습 해보기 →</span>
      </div>
    </button>
  );
}
