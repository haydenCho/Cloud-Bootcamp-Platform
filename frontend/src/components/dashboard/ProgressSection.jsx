import { useEffect, useMemo, useState } from 'react';
import { getUnits } from '../../api/unit';
import { getProgress } from '../../api/progress';
import { getMockProgress } from '../../mocks/mockProgress';
import ProgressCard from './ProgressCard';

/**
 * 진도 표시 섹션.
 * - 단원 목록: 실제 공개 API(GET /api/v1/units)
 * - GENERAL 단원 진도: 실제 API(GET /api/v1/progress) — { generalPercent, blankPercent }
 * - PRACTICE 단원 진도: 아직 mission_progress API 가 없어 mockProgress 더미 유지
 *   → 5단계에서 실제 값으로 교체 예정.
 */
export default function ProgressSection() {
  const [units, setUnits] = useState([]);
  const [progressList, setProgressList] = useState([]); // GET /progress 결과 (GENERAL)
  const [status, setStatus] = useState('loading'); // loading | done | error

  useEffect(() => {
    let alive = true;
    // 단원 목록 + 실제 진도(GENERAL) 를 함께 로드. 진도 실패는 치명적이지 않게 처리.
    Promise.all([getUnits(), getProgress().catch(() => [])])
      .then(([unitList, progress]) => {
        if (!alive) return;
        setUnits(unitList);
        setProgressList(progress);
        setStatus('done');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, []);

  // PRACTICE 단원용 더미(⚠️ 5단계에서 교체 예정)
  const mockMap = useMemo(() => getMockProgress(units), [units]);

  // GENERAL 단원은 실제 진도로 대체, PRACTICE 는 더미 유지
  const progressMap = useMemo(() => {
    const realByCode = {};
    for (const p of progressList) {
      realByCode[p.unitCode] = {
        type: 'GENERAL',
        generalPercent: p.generalPercent,
        blankPercent: p.blankPercent,
      };
    }
    const map = {};
    for (const unit of units) {
      if (unit.type === 'GENERAL') {
        // 실제 진도(없으면 0%)
        map[unit.code] =
          realByCode[unit.code] ?? { type: 'GENERAL', generalPercent: 0, blankPercent: 0 };
      } else {
        // PRACTICE — 더미 유지
        map[unit.code] = mockMap[unit.code];
      }
    }
    return map;
  }, [units, progressList, mockMap]);

  return (
    <section>
      <div className="mb-4 flex items-baseline justify-between">
        <h2 className="text-xl font-bold text-dark">학습 진도</h2>
        <span className="text-xs text-dark/50">※ 실습(PRACTICE) 진도는 아직 더미입니다</span>
      </div>

      {status === 'loading' && <p className="py-10 text-center text-slate-400">불러오는 중...</p>}
      {status === 'error' && (
        <p className="py-10 text-center text-red-500">단원 목록을 불러오지 못했습니다.</p>
      )}

      {status === 'done' && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {units.map((unit) => (
            <ProgressCard key={unit.id} unit={unit} progress={progressMap[unit.code]} />
          ))}
        </div>
      )}
    </section>
  );
}
