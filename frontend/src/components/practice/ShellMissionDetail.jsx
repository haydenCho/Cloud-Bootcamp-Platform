import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { verifyMission } from '../../api/mission';
import { simulateShell } from '../../mocks/simulateShell';

/**
 * SHELL 타입 미션 상세 = 가짜 터미널.
 * - 사용자가 명령을 입력해 Enter → POST verify.
 * - 정답이면 simulateShell(입력)의 가짜 출력을 보여주고 완료 처리(백엔드는 패턴 검증만).
 * - 오답이면 안내 메시지 후 재시도 가능.
 * - 비로그인 시 제출은 로그인 유도.
 *
 * (다른 유형(PYTHON/DB/DOCKER/K8S)은 각자 전용 상세 컴포넌트를 만들어
 *  PracticeUnitPage 에서 missionType 으로 분기하면 된다.)
 */
export default function ShellMissionDetail({ mission, loggedIn, onBack, onCompleted }) {
  // lines: { kind: 'cmd' | 'out' | 'ok' | 'err', text }
  const [lines, setLines] = useState([
    { kind: 'out', text: '# 미션에 맞는 셸 명령을 입력하고 Enter 를 누르세요.' },
  ]);
  const [input, setInput] = useState('');
  const [completed, setCompleted] = useState(mission.completed);
  const [submitting, setSubmitting] = useState(false);

  const scrollRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    // 새 줄 추가 시 맨 아래로 스크롤
    scrollRef.current?.scrollTo(0, scrollRef.current.scrollHeight);
  }, [lines]);

  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  function append(newLines) {
    setLines((prev) => [...prev, ...newLines]);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const command = input;
    if (!command.trim() || submitting) return;

    if (!loggedIn) {
      append([
        { kind: 'cmd', text: command },
        { kind: 'err', text: '로그인 후 제출할 수 있습니다.' },
      ]);
      setInput('');
      return;
    }

    setSubmitting(true);
    try {
      const res = await verifyMission(mission.id, command);
      if (res.correct) {
        const output = simulateShell(command).map((t) => ({ kind: 'out', text: t }));
        append([
          { kind: 'cmd', text: command },
          ...output,
          { kind: 'ok', text: `✓ 미션 완료!  +${res.xpReward} XP` },
        ]);
        setInput('');
        if (!completed) {
          setCompleted(true);
          onCompleted?.(mission.id);
        }
      } else {
        append([
          { kind: 'cmd', text: command },
          { kind: 'err', text: '명령어가 올바르지 않습니다. 다시 시도해보세요.' },
        ]);
        setInput(''); // 오답이면 새 입력 라인을 비워둔다(터미널 과정은 유지)
      }
    } catch (_) {
      append([{ kind: 'err', text: '검증 요청에 실패했습니다.' }]);
    } finally {
      setSubmitting(false);
      inputRef.current?.focus();
    }
  }

  const lineColor = {
    cmd: 'text-slate-100',
    out: 'text-slate-300',
    ok: 'text-accent',
    err: 'text-red-400',
  };

  return (
    <div>
      <button onClick={onBack} className="mb-4 text-sm text-secondary hover:text-primary">
        ← 미션 목록
      </button>

      <div className="mb-4 flex items-start justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold text-dark">{mission.title}</h2>
          <p className="mt-1 text-sm text-dark/70">{mission.description}</p>
        </div>
        {completed && (
          <span className="shrink-0 rounded-full bg-accent/20 px-3 py-1 text-sm font-bold text-[#8a7400]">
            완료 ✓
          </span>
        )}
      </div>

      {!loggedIn && (
        <div className="mb-3 rounded-lg border border-secondary/30 bg-secondary/10 px-4 py-2 text-sm text-dark/70">
          미션 제출은 로그인 후 이용할 수 있습니다.{' '}
          <Link to="/login" className="font-semibold text-primary underline">
            로그인하기
          </Link>
        </div>
      )}

      {/* 가짜 터미널 */}
      <div className="overflow-hidden rounded-xl border border-dark/20 bg-dark shadow-sm">
        <div className="flex items-center gap-1.5 border-b border-white/10 px-4 py-2">
          <span className="h-3 w-3 rounded-full bg-red-400/80" />
          <span className="h-3 w-3 rounded-full bg-yellow-400/80" />
          <span className="h-3 w-3 rounded-full bg-green-400/80" />
          <span className="ml-2 text-xs text-slate-400">bash — 시뮬레이터</span>
        </div>

        <div ref={scrollRef} className="max-h-80 overflow-y-auto px-4 py-3 font-mono text-sm leading-6">
          {lines.map((l, i) => (
            <div key={i} className={lineColor[l.kind]}>
              {l.kind === 'cmd' ? <span className="text-green-400">guest@sim:~$ </span> : null}
              <span className="whitespace-pre-wrap break-words">{l.text}</span>
            </div>
          ))}

          {/* 입력 라인 */}
          <form onSubmit={handleSubmit} className="mt-1 flex items-center">
            <span className="text-green-400">guest@sim:~$&nbsp;</span>
            <input
              ref={inputRef}
              type="text"
              value={input}
              disabled={submitting}
              onChange={(e) => setInput(e.target.value)}
              spellCheck={false}
              autoCapitalize="off"
              autoComplete="off"
              className="flex-1 bg-transparent font-mono text-sm text-slate-100 outline-none"
              placeholder="명령어 입력..."
            />
          </form>
        </div>
      </div>

      {/* 완료 시 목록으로 돌아가 다음 문제를 바로 풀 수 있게 */}
      {completed && (
        <div className="mt-6 text-center">
          <button
            onClick={onBack}
            className="rounded-lg bg-primary px-6 py-2 font-semibold text-white transition hover:bg-light"
          >
            목록으로
          </button>
        </div>
      )}
    </div>
  );
}
