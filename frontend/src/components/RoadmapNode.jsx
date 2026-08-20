import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * 로드맵의 단원 하나 = 원형 아이콘 + 아래 이름.
 * 아이콘 이미지는 unit.iconImagePath(/assets/imgs/roadmap/{code}.png)로 찾고,
 * 파일이 없으면 onError 로 단원 이름 첫 글자 플레이스홀더를 보여준다.
 * → 나중에 그 경로에 실제 이미지 파일만 넣으면 코드 수정 없이 반영된다.
 *
 * 클릭 시 /units/:code 로 이동한다.
 */
export default function RoadmapNode({ unit }) {
  const navigate = useNavigate();
  const [imgFailed, setImgFailed] = useState(false);

  const isPractice = unit.type === 'PRACTICE';
  // 일반 학습 vs 실습을 테두리 색으로 구분 (light 톤 팔레트)
  const ringColor = isPractice ? 'border-secondary' : 'border-primary';
  const initial = unit.name?.trim().charAt(0) ?? '?';

  return (
    <button
      type="button"
      onClick={() => navigate(`/units/${unit.code}`)}
      className="group flex w-28 flex-col items-center gap-2 focus:outline-none"
      title={unit.name}
    >
      <span
        className={`flex h-20 w-20 items-center justify-center rounded-full border-2 ${ringColor}
                    bg-white shadow-sm transition-all duration-200
                    group-hover:scale-110 group-hover:border-accent group-hover:shadow-md`}
      >
        {unit.iconImagePath && !imgFailed ? (
          <img
            src={unit.iconImagePath}
            alt={unit.name}
            className="h-11 w-11 object-contain"
            onError={() => setImgFailed(true)}
          />
        ) : (
          <span className="text-2xl font-bold text-primary">{initial}</span>
        )}
      </span>

      <span className="text-center text-sm font-medium text-dark">{unit.name}</span>
      {isPractice && (
        <span className="rounded-full bg-secondary/15 px-2 py-0.5 text-[11px] font-medium text-secondary">
          실습
        </span>
      )}
    </button>
  );
}
