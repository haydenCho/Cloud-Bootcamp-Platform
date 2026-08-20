import { useEffect, useState } from 'react';
import { getUnits } from '../api/unit';
import StudyCard from '../components/study/StudyCard';

/**
 * 학습하기(/study). 로드맵의 그래픽 느낌과 달리, 단원을 카드/목록 형태로 나열하는
 * "공부를 위한" 콘텐츠 목록집. GET /api/v1/units(콘텐츠/빈칸/미션 보조 정보 포함)를 사용한다.
 * 카드 클릭 시 /units/:code 로 이동(로드맵과 동일).
 */
export default function StudyPage() {
  const [units, setUnits] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | done | error

  useEffect(() => {
    let alive = true;
    getUnits()
      .then((data) => {
        if (!alive) return;
        setUnits(data);
        setStatus('done');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, []);

  return (
    <div className="mx-auto max-w-3xl px-4 py-10">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-dark">학습하기</h1>
        <p className="mt-2 text-dark/60">
          단원을 클릭해 학습을 시작하세요. 로그인하면 진도가 저장됩니다.
        </p>
      </div>

      {status === 'loading' && <p className="py-16 text-center text-slate-400">불러오는 중...</p>}
      {status === 'error' && (
        <p className="py-16 text-center text-red-500">단원 목록을 불러오지 못했습니다.</p>
      )}

      {status === 'done' && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {units.map((unit) => (
            <StudyCard key={unit.id} unit={unit} />
          ))}
        </div>
      )}
    </div>
  );
}
