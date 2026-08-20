import DOMPurify from 'dompurify';

/**
 * 학습/실습 본문(HTML) 렌더링 공통 컴포넌트.
 * - content API 로 받은 HTML 을 DOMPurify 로 sanitize 후 렌더링만 한다(진도 추적 등 부가 로직 없음).
 * - LearnSection(GENERAL 학습, 스크롤 진도 포함)과 PracticeNoteSection(실습 노트, 진도 없음)이 공유한다.
 */
export default function NoteArticle({ html, className = '' }) {
  const safeHtml = DOMPurify.sanitize(html);
  return (
    <article
      className={`learn-content leading-relaxed text-dark/90 ${className}`}
      dangerouslySetInnerHTML={{ __html: safeHtml }}
    />
  );
}
