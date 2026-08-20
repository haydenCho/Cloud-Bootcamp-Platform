import { useEffect, useState } from 'react';
import { getContent } from '../../api/content';
import NoteArticle from '../learn/NoteArticle';

/**
 * 미션 풀이 화면(실습 창) 하단에 노출하는 실습 노트(블로그 글 형태).
 * - content 테이블은 GENERAL 전용이 아니므로 PRACTICE 단원 code 로도 그대로 조회된다.
 * - 같은 단원의 여러 미션이 노트를 공통으로 보므로 미션이 아니라 unit 단위로 한 번만 불러온다.
 * - 학습 진도(LearnSection)와 달리 스크롤 진도를 기록하지 않는다(실습 진도는 미션 완료로만 집계).
 * - 노트가 없는 단원(아직 콘텐츠를 안 채운 실습)은 조용히 아무것도 보여주지 않는다.
 */
export default function PracticeNoteSection({ unit }) {
  const [content, setContent] = useState(null);
  const [status, setStatus] = useState('loading'); // loading | done | empty | error

  useEffect(() => {
    let alive = true;
    setStatus('loading');
    getContent(unit.code)
      .then((data) => {
        if (!alive) return;
        setContent(data);
        setStatus('done');
      })
      .catch((err) => {
        if (!alive) return;
        setStatus(err.message?.includes('콘텐츠') ? 'empty' : 'error');
      });
    return () => {
      alive = false;
    };
  }, [unit.code]);

  if (status !== 'done') return null;

  return (
    <div className="mt-10 border-t border-slate-200 pt-8">
      <h2 className="mb-4 text-lg font-bold text-dark">실습 노트</h2>
      <NoteArticle html={content.body} />
    </div>
  );
}
