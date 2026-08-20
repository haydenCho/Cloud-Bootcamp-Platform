import { useState } from 'react';
import { submitBlankAnswer } from '../../api/blank';

/**
 * 빈칸 문제 1개.
 * - sentence_template 의 첫 "{blank}" 를 <input> 으로 치환.
 * - 엔터 시 채점: 정답 초록 테두리 / 오답 빨강 테두리 + 아래 회색 작은 글씨로 정답 표시.
 * - 오답 후 다시 입력하면 재채점 가능.
 * - 비로그인 시 제출하면 로그인 유도(onNeedLogin).
 *
 * 초기 상태(initial)는 GET blanks 로 복원된 { userAnswer, isCorrect } (없으면 null).
 * ※ 복원 시 정답 문자열은 서버가 주지 않으므로(치팅 방지), 오답 복원은 빨강 테두리만 표시하고
 *    회색 정답 힌트는 재제출 시 다시 나타난다.
 */
export default function BlankQuestion({ question, index, loggedIn, onResult, onNeedLogin }) {
  const [value, setValue] = useState(question.userAnswer ?? '');
  // status: 'idle' | 'correct' | 'wrong'
  const [status, setStatus] = useState(
    question.isCorrect === true ? 'correct' : question.isCorrect === false ? 'wrong' : 'idle',
  );
  const [correctAnswer, setCorrectAnswer] = useState(null); // 재제출 시에만 채워짐
  const [submitting, setSubmitting] = useState(false);

  const parts = question.sentenceTemplate.split('{blank}');
  const before = parts[0] ?? '';
  const after = parts.slice(1).join('{blank}'); // 여분 placeholder 는 텍스트로 유지

  async function handleKeyDown(e) {
    if (e.key !== 'Enter') return;
    e.preventDefault();
    if (!value.trim()) return;
    if (!loggedIn) {
      onNeedLogin?.();
      return;
    }
    setSubmitting(true);
    try {
      const res = await submitBlankAnswer(question.id, value);
      setStatus(res.isCorrect ? 'correct' : 'wrong');
      setCorrectAnswer(res.isCorrect ? null : res.correctAnswer);
      onResult?.(question.id, res.isCorrect);
    } catch (_) {
      // 인증 만료 등 — 조용히 무시(테두리 변화 없음)
    } finally {
      setSubmitting(false);
    }
  }

  const borderClass =
    status === 'correct'
      ? 'border-green-500 focus:border-green-500 focus:ring-green-500'
      : status === 'wrong'
        ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
        : 'border-slate-300 focus:border-primary focus:ring-primary';

  return (
    <li className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex items-start gap-2">
        <span className="mt-1 shrink-0 text-sm font-semibold text-primary">Q{index + 1}.</span>
        <p className="leading-8 text-dark/90">
          {before}
          <input
            type="text"
            value={value}
            disabled={submitting}
            onChange={(e) => {
              setValue(e.target.value);
              if (status !== 'idle') setStatus('idle'); // 다시 입력하면 초기화(재채점 준비)
            }}
            onKeyDown={handleKeyDown}
            placeholder="정답 입력 후 Enter"
            className={`mx-1 inline-block w-40 rounded-md border px-2 py-1 text-sm outline-none transition focus:ring-1 ${borderClass}`}
          />
          {after}
        </p>
      </div>

      {/* 오답 시 정답 회색 표시 (재제출로 얻은 경우) */}
      {status === 'wrong' && correctAnswer && (
        <p className="mt-1 pl-8 text-xs text-slate-400">정답: {correctAnswer}</p>
      )}
      {status === 'correct' && (
        <p className="mt-1 pl-8 text-xs text-green-600">정답입니다 ✓</p>
      )}
    </li>
  );
}
