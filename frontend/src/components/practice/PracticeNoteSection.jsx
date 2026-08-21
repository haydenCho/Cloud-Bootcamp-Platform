import { useEffect, useState } from 'react';
import { getChapters, getChapter } from '../../api/chapters';
import NoteArticle from '../learn/NoteArticle';

/**
 * 실습 노트(블로그 글 형태). 8단계에서 content 가 챕터로 분리되었으므로,
 * 챕터 목록을 받아 각 챕터 본문을 이어붙여 한 흐름으로 보여준다(실습 UX 는 기존과 동일한 단일 스크롤).
 * - content 는 GENERAL 전용이 아니므로 PRACTICE 단원 code 로도 그대로 조회된다.
 * - 노트가 없는 단원은 조용히 아무것도 보여주지 않는다.
 */
export default function PracticeNoteSection({ unit, showHeading = true }) {
  const [bodies, setBodies] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | done | empty | error

  useEffect(() => {
    let alive = true;
    setStatus('loading');
    getChapters(unit.code)
      .then((chapters) => {
        if (!alive) return [];
        if (!chapters || chapters.length === 0) {
          setStatus('empty');
          return [];
        }
        return Promise.all(chapters.map((c) => getChapter(unit.code, c.sortOrder)));
      })
      .then((details) => {
        if (!alive || !details) return;
        if (details.length > 0) {
          setBodies(details.map((d) => ({ id: d.id, body: d.body })));
          setStatus('done');
        }
      })
      .catch(() => {
        if (alive) setStatus('error');
      });
    return () => {
      alive = false;
    };
  }, [unit.code]);

  if (status !== 'done') return null;

  return (
    <div className={showHeading ? 'mt-10 border-t border-slate-200 pt-8' : ''}>
      {showHeading && <h2 className="mb-4 text-lg font-bold text-dark">실습 노트</h2>}
      {bodies.map((b) => (
        <NoteArticle key={b.id} html={b.body} />
      ))}
    </div>
  );
}
