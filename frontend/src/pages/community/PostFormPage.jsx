import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createPost, getPost, updatePost } from '../../api/community';
import { useAuth } from '../../store/authStore';

/**
 * 게시글 작성/수정 폼 (제목 + 본문 textarea, 일반 텍스트).
 * :id 파라미터가 있으면 수정 모드.
 */
export default function PostFormPage() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();
  const { user } = useAuth();

  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(isEdit);
  const [forbidden, setForbidden] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    let alive = true;
    getPost(id)
      .then((post) => {
        if (!alive) return;
        if (post.authorId !== user?.id) {
          setForbidden(true);
        } else {
          setTitle(post.title);
          setBody(post.body);
        }
      })
      .catch((err) => alive && setError(err.message))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [id, isEdit, user?.id]);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    if (!title.trim() || !body.trim()) {
      setError('제목과 본문을 입력해주세요.');
      return;
    }
    setSaving(true);
    try {
      const saved = isEdit
        ? await updatePost(id, { title, body })
        : await createPost({ title, body });
      navigate(`/community/${saved.id}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p className="py-16 text-center text-slate-400">불러오는 중...</p>;
  if (forbidden)
    return <p className="py-24 text-center text-red-500">본인 글만 수정할 수 있습니다.</p>;

  return (
    <div className="mx-auto max-w-3xl px-4 py-10">
      <h1 className="mb-6 text-2xl font-extrabold text-dark">{isEdit ? '글 수정' : '글쓰기'}</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="제목"
          maxLength={200}
          className="w-full rounded-lg border border-slate-300 bg-white px-4 py-2 text-dark outline-none transition focus:border-primary focus:ring-1 focus:ring-primary"
        />
        <textarea
          value={body}
          onChange={(e) => setBody(e.target.value)}
          placeholder="본문을 입력하세요. (줄바꿈 지원)"
          rows={14}
          className="w-full resize-y rounded-lg border border-slate-300 bg-white px-4 py-3 text-dark outline-none transition focus:border-primary focus:ring-1 focus:ring-primary"
        />

        {error && <p className="text-sm text-red-500">{error}</p>}

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={saving}
            className="rounded-lg bg-primary px-5 py-2 font-semibold text-white transition hover:bg-light disabled:opacity-50"
          >
            {saving ? '저장 중...' : isEdit ? '수정' : '등록'}
          </button>
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="rounded-lg border border-slate-300 px-5 py-2 font-medium text-dark/70 transition hover:border-primary hover:text-primary"
          >
            취소
          </button>
        </div>
      </form>
    </div>
  );
}
