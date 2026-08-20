import { useEffect, useState } from 'react';
import { getUnits } from '../api/unit';
import RoadmapNode from '../components/RoadmapNode';

const COLS = 6; // 한 줄에 놓을 단원 수 (가로 방향 스네이크 레이아웃)

function chunk(arr, size) {
  const rows = [];
  for (let i = 0; i < arr.length; i += size) rows.push(arr.slice(i, i + size));
  return rows;
}

/**
 * 메인홈 = 학습 로드맵. 공개 API(GET /api/v1/units)로 단원을 받아
 * 가로 방향 스네이크(행마다 방향 교차) 형태로 정적 렌더링한다. 페이지 스크롤 없이 한 화면에 담는다.
 * (진입 애니메이션 / 마우스 확대 효과는 7단계 몫)
 */
export default function RoadmapPage() {
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
      .catch(() => {
        if (alive) setStatus('error');
      });
    return () => {
      alive = false;
    };
  }, []);

  const rows = chunk(units, COLS);

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center px-4 py-6">
      {status === 'loading' && <p className="text-slate-400">로드맵을 불러오는 중...</p>}
      {status === 'error' && (
        <p className="text-red-500">로드맵을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.</p>
      )}

      {status === 'done' && (
        <div className="max-w-full overflow-x-auto">
          <div className="mx-auto flex w-max flex-col items-center gap-10">
            {rows.map((row, r) => (
              <div
                key={r}
                className={`flex items-start ${r % 2 === 1 ? 'flex-row-reverse' : ''}`}
              >
                {row.map((unit, i) => {
                  // 완전한 일직선 대신 부드러운 파동으로 높이를 약간씩 다르게(자연스러운 느낌)
                  const globalIndex = r * COLS + i;
                  const offsetY = Math.round(12 * Math.sin(globalIndex * 0.8));
                  return (
                    <div
                      key={unit.id}
                      className="flex items-start"
                      style={{ transform: `translateY(${offsetY}px)` }}
                    >
                      <RoadmapNode unit={unit} />
                      {i < row.length - 1 && (
                        // 단원 사이 가로 연결선 (아이콘 원 중심 높이에 맞춤)
                        <span className="mt-10 h-0.5 w-6 shrink-0 bg-secondary/40 sm:w-10" />
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
