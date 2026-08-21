import { memo } from 'react';
import DOMPurify from 'dompurify';

/**
 * 학습/실습 본문(HTML) 렌더링 공통 컴포넌트.
 * - 챕터 본문 HTML 을 DOMPurify 로 sanitize 후 렌더링만 한다(목차/진도 등 부가 로직은 부모가 담당).
 * - ChapterPage(챕터 본문)와 PracticeNoteSection(실습 노트)이 공유한다.
 * - memo 로 감싸 부모 리렌더(예: 목차 activeId 변경)에도 innerHTML 을 다시 만들지 않게 한다.
 *   그래야 부모가 본문에 심은 heading id / IntersectionObserver 대상 노드가 떨어져 나가지 않는다.
 */
function NoteArticle({ html, className = '' }) {
  const safeHtml = DOMPurify.sanitize(html);
  return (
    <article
      className={`learn-content leading-relaxed text-dark/90 ${className}`}
      dangerouslySetInnerHTML={{ __html: safeHtml }}
    />
  );
}

export default memo(NoteArticle);
