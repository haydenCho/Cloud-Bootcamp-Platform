import { Link } from 'react-router-dom';

/** 이후 단계에서 채울 빈 페이지용 플레이스홀더. */
export default function ComingSoon({ title, note }) {
  return (
    <div className="mx-auto max-w-2xl px-4 py-24 text-center">
      <h1 className="text-2xl font-bold text-dark">{title}</h1>
      <p className="mt-3 text-dark/60">{note ?? '추후 구현 예정입니다.'}</p>
      <Link
        to="/"
        className="mt-8 inline-block rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-light"
      >
        로드맵으로 돌아가기
      </Link>
    </div>
  );
}
