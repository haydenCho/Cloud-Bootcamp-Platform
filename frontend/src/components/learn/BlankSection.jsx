import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getBlanks } from '../../api/blank';
import { useAuth } from '../../store/authStore';
import BlankQuestion from './BlankQuestion';

/**
 * 빈칸 채우기 섹션.
 * - 문제 목록을 불러와 렌더링(로그인 시 이전 답안/정답 여부 복원).
 * - 모든 문제가 정답이면 "빈칸 채우기 완료" 표시 (별도 완료 플래그 없이 프론트에서 계산).
 * - 비로그인 시 제출은 로그인 유도.
 */
export default function BlankSection({ unit }) {
  const { user } = useAuth();
  const loggedIn = !!user;

  const [questions, setQuestions] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | done | error
  const [correctMap, setCorrectMap] = useState({}); // id -> boolean
  const [showLoginHint, setShowLoginHint] = useState(false);

  useEffect(() => {
    let alive = true;
    setStatus('loading');
    getBlanks(unit.code)
      .then((list) => {
        if (!alive) return;
        setQuestions(list);
        const init = {};
        list.forEach((q) => {
          init[q.id] = q.isCorrect === true;
        });
        setCorrectMap(init);
        setStatus('done');
      })
      .catch(() => alive && setStatus('error'));
    return () => {
      alive = false;
    };
  }, [unit.code]);

  const allCorrect = useMemo(
    () => questions.length > 0 && questions.every((q) => correctMap[q.id]),
    [questions, correctMap],
  );

  function handleResult(id, isCorrect) {
    setCorrectMap((prev) => ({ ...prev, [id]: isCorrect }));
  }

  if (status === 'loading') return <p className="py-16 text-center text-slate-400">불러오는 중...</p>;
  if (status === 'error') return <p className="py-16 text-center text-red-500">문제를 불러오지 못했습니다.</p>;
  if (questions.length === 0)
    return <p className="py-16 text-center text-dark/50">아직 등록된 빈칸 문제가 없습니다.</p>;

  const correctCount = questions.filter((q) => correctMap[q.id]).length;

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <p className="text-sm text-dark/60">
          정답 <span className="font-semibold text-primary">{correctCount}</span> / {questions.length}
        </p>
        {allCorrect && (
          <span className="rounded-full bg-accent/20 px-3 py-1 text-sm font-bold text-[#8a7400]">
            빈칸 채우기 완료 🎉
          </span>
        )}
      </div>

      {!loggedIn && showLoginHint && (
        <div className="mb-4 rounded-lg border border-secondary/30 bg-secondary/10 px-4 py-2 text-sm text-dark/70">
          답안 제출은 로그인 후 이용할 수 있습니다.{' '}
          <Link to="/login" className="font-semibold text-primary underline">
            로그인하기
          </Link>
        </div>
      )}

      <ul className="space-y-3">
        {questions.map((q, i) => (
          <BlankQuestion
            key={q.id}
            question={q}
            index={i}
            loggedIn={loggedIn}
            onResult={handleResult}
            onNeedLogin={() => setShowLoginHint(true)}
          />
        ))}
      </ul>
    </div>
  );
}
