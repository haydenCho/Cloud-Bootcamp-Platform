import { useState } from 'react';
import { Link } from 'react-router-dom';

/**
 * 학습하기(/study) 단원 카드 — 블로그 글 목록/갤러리 느낌.
 * 아이콘 + 단원명 + 유형 + 보조 정보(콘텐츠/빈칸/미션 존재 여부).
 * 콘텐츠가 아직 없는 단원은 "준비 중"으로 자연스럽게 표시한다. 클릭 시 /units/:code 로 이동.
 */
export default function StudyCard({ unit }) {
  const [imgFailed, setImgFailed] = useState(false);
  const isPractice = unit.type === 'PRACTICE';

  // 보조 정보 문구 + 준비중 여부
  let info;
  let pending = false;
  if (isPractice) {
    // 미션이 있으면 미션 개수, 없어도 실습 노트가 있으므로 "노트"로 안내한다(준비 중/흐림 제거).
    if (unit.missionCount > 0) {
      info = `실습 미션 ${unit.missionCount}개`;
    } else if (unit.hasContent) {
      info = '실습 노트';
    } else {
      info = '실습 준비 중';
      pending = true;
    }
  } else {
    const parts = [];
    if (unit.hasContent) parts.push('학습 콘텐츠');
    if (unit.blankCount > 0) parts.push(`빈칸 ${unit.blankCount}문제`);
    info = parts.length > 0 ? parts.join(' · ') : '콘텐츠 준비 중';
    pending = parts.length === 0;
  }

  const ringColor = isPractice ? 'border-secondary' : 'border-primary';
  const initial = unit.name?.trim().charAt(0) ?? '?';
  // GENERAL 은 챕터 인덱스(/study/:code), PRACTICE 는 실습 페이지(/units/:code).
  const target = isPractice ? `/units/${unit.code}` : `/study/${unit.code}`;

  return (
    <Link
      to={target}
      className={`flex items-center gap-4 rounded-2xl border bg-white p-4 shadow-sm transition
                  hover:-translate-y-0.5 hover:shadow-md ${
                    pending ? 'border-slate-200 opacity-70' : 'border-slate-200 hover:border-primary'
                  }`}
    >
      {/* 아이콘 */}
      <span
        className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-full border-2 bg-white ${ringColor}`}
      >
        {unit.iconImagePath && !imgFailed ? (
          <img
            src={unit.iconImagePath}
            alt={unit.name}
            className="h-8 w-8 object-contain"
            onError={() => setImgFailed(true)}
          />
        ) : (
          <span className="text-lg font-bold text-primary">{initial}</span>
        )}
      </span>

      {/* 텍스트 */}
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2">
          <h3 className="truncate font-bold text-dark">{unit.name}</h3>
          <span
            className={`shrink-0 rounded-full px-2 py-0.5 text-[11px] font-medium ${
              isPractice ? 'bg-secondary/15 text-secondary' : 'bg-primary/10 text-primary'
            }`}
          >
            {isPractice ? '실습' : '일반 학습'}
          </span>
        </div>
        <p className={`mt-0.5 text-sm ${pending ? 'text-dark/40' : 'text-dark/60'}`}>{info}</p>
      </div>

      <span className="shrink-0 text-sm text-dark/30">→</span>
    </Link>
  );
}
