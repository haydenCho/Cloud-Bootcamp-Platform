import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMissions } from '../../api/mission';
import { useAuth } from '../../store/authStore';
import MissionGallery from './MissionGallery';
import PracticeNoteSection from './PracticeNoteSection';
import ShellMissionDetail from './ShellMissionDetail';

/**
 * PRACTICE 단원 공통 페이지.
 * - 갤러리(미션 목록) ↔ 미션 상세("실습 해보기")를 로컬 상태로 전환한다.
 * - 미션 상세는 missionType 으로 분기한다(현재 SHELL 만 구현, 나머지는 준비 중 안내).
 * - 어떤 PRACTICE 단원(리눅스/쉘/도커/K8s ...)이든 이 컴포넌트로 재사용된다.
 */
export default function PracticeUnitPage({ unit }) {
  const { user } = useAuth();
  const loggedIn = !!user;

  const [missions, setMissions] = useState([]);
  const [totalXp, setTotalXp] = useState(0);
  const [status, setStatus] = useState('loading'); // loading | done | error
  const [selectedId, setSelectedId] = useState(null);

  useEffect(() => {
    let alive = true;
    setStatus('loading');
    getMissions(unit.code)
      .then((data) => {
        if (!alive) return;
        setMissions(data.missions);
        setTotalXp(data.totalXp);
        setStatus('done');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, [unit.code]);

  const earnedXp = useMemo(
    () => missions.filter((m) => m.completed).reduce((sum, m) => sum + m.xpReward, 0),
    [missions],
  );
  const selected = useMemo(
    () => missions.find((m) => m.id === selectedId) ?? null,
    [missions, selectedId],
  );

  function handleCompleted(id) {
    setMissions((prev) => prev.map((m) => (m.id === id ? { ...m, completed: true } : m)));
  }

  const header = (
    <div className="mb-6">
      <Link to="/" className="text-sm text-secondary hover:text-primary">
        ← 로드맵
      </Link>
      {/* unit.name 이 이미 "…(실습)" 이므로 뒤에 "실습"을 덧붙이지 않는다. */}
      <h1 className="mt-2 text-2xl font-extrabold text-dark">{unit.name}</h1>
    </div>
  );

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      {header}

      {status === 'loading' && <p className="py-16 text-center text-slate-400">불러오는 중...</p>}
      {status === 'error' && (
        <p className="py-16 text-center text-red-500">미션을 불러오지 못했습니다.</p>
      )}

      {/* 미션이 아직 없는 실습 단원: 실습 노트를 블로그 형식으로 전체 렌더링 */}
      {status === 'done' && missions.length === 0 && (
        <PracticeNoteSection unit={unit} showHeading={false} />
      )}

      {status === 'done' &&
        missions.length > 0 &&
        (selected ? (
          <div>
            <MissionDetailDispatcher
              mission={selected}
              loggedIn={loggedIn}
              onBack={() => setSelectedId(null)}
              onCompleted={handleCompleted}
            />
            {/* 실습 노트(블로그 글) — 같은 단원의 미션이면 공통으로 노출 */}
            <PracticeNoteSection unit={unit} />
          </div>
        ) : (
          <MissionGallery
            unit={unit}
            missions={missions}
            earnedXp={earnedXp}
            totalXp={totalXp}
            onSelect={(m) => setSelectedId(m.id)}
          />
        ))}
    </div>
  );
}

/** missionType 별 상세 화면 분기. 새 유형은 여기에 case 를 추가한다. */
function MissionDetailDispatcher({ mission, loggedIn, onBack, onCompleted }) {
  switch (mission.missionType) {
    case 'SHELL':
      return (
        <ShellMissionDetail
          mission={mission}
          loggedIn={loggedIn}
          onBack={onBack}
          onCompleted={onCompleted}
        />
      );
    default:
      return (
        <div>
          <button onClick={onBack} className="mb-4 text-sm text-secondary hover:text-primary">
            ← 미션 목록
          </button>
          <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-dark/60">
            <p className="font-semibold text-dark">{mission.title}</p>
            <p className="mt-2 text-sm">
              이 실습 유형({mission.missionType})은 아직 준비 중입니다.
            </p>
          </div>
        </div>
      );
  }
}
