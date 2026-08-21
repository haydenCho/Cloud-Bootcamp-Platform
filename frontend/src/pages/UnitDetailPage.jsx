import { useEffect, useState } from 'react';
import { Navigate, useParams } from 'react-router-dom';
import { getUnits } from '../api/unit';
import ComingSoon from '../components/ComingSoon';
import PracticeUnitPage from '../components/practice/PracticeUnitPage';

/**
 * 단원 상세. 단원 type 에 따라 분기한다.
 * - GENERAL: 챕터 기반 학습으로 이동(/study/:code — 8단계 개선). 로드맵/기존 링크 호환용 리다이렉트.
 * - PRACTICE: 실습 시뮬레이터(미션 + 실습 노트).
 *
 * 단원 type 을 알기 위해 별도 단일 조회 API 대신 공개 목록(GET /api/v1/units)에서 code 로 찾는다.
 */
export default function UnitDetailPage() {
  const { code } = useParams();
  const [unit, setUnit] = useState(null);
  const [status, setStatus] = useState('loading'); // loading | done | notfound | error

  useEffect(() => {
    let alive = true;
    setStatus('loading');
    getUnits()
      .then((units) => {
        if (!alive) return;
        const found = units.find((u) => u.code === code);
        setUnit(found ?? null);
        setStatus(found ? 'done' : 'notfound');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, [code]);

  if (status === 'loading') {
    return <p className="py-24 text-center text-slate-400">불러오는 중...</p>;
  }
  if (status === 'error') {
    return <p className="py-24 text-center text-red-500">단원 정보를 불러오지 못했습니다.</p>;
  }
  if (status === 'notfound') {
    return <ComingSoon title="단원을 찾을 수 없습니다" note={`'${code}' 단원이 존재하지 않습니다.`} />;
  }

  if (unit.type === 'PRACTICE') {
    // 실습 갤러리 + 미션 상세 (5단계). SHELL 유형만 실제 구현, 나머지 유형은 준비 중 안내.
    return <PracticeUnitPage unit={unit} />;
  }

  // GENERAL 은 챕터 기반 학습 인덱스로 이동(로드맵/기존 /units 링크 호환).
  return <Navigate to={`/study/${unit.code}`} replace />;
}
