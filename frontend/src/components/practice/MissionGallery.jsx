import MissionCard from './MissionCard';

/**
 * 실습 미션 갤러리. 미션 카드 그리드 + 단원 XP 진행 요약.
 */
export default function MissionGallery({ unit, missions, earnedXp, totalXp, onSelect }) {
  const doneCount = missions.filter((m) => m.completed).length;

  return (
    <div>
      <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-dark/60">실습 진행</p>
            <p className="text-lg font-bold text-dark">
              {doneCount} / {missions.length} 미션 완료
            </p>
          </div>
          <div className="text-right">
            <p className="text-sm text-dark/60">획득 XP</p>
            <p className="text-lg font-bold text-secondary">
              {earnedXp} <span className="text-sm font-normal text-dark/40">/ {totalXp}</span>
            </p>
          </div>
        </div>
        <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-slate-100">
          <div
            className="h-full rounded-full bg-accent transition-all"
            style={{ width: `${totalXp ? Math.round((earnedXp / totalXp) * 100) : 0}%` }}
          />
        </div>
      </div>

      {missions.length === 0 ? (
        <p className="py-16 text-center text-dark/50">아직 등록된 실습 미션이 없습니다.</p>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {missions.map((m, i) => (
            <MissionCard key={m.id} mission={m} index={i} onSelect={onSelect} />
          ))}
        </div>
      )}
    </div>
  );
}
