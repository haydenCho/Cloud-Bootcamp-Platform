import { useState } from 'react';
import { updateProfile } from '../../api/user';
import { useAuth, setUser } from '../../store/authStore';

/**
 * 계정 관리 섹션 — 여기는 더미가 아니라 실제 API 연동.
 * 1단계의 PATCH /api/v1/users/me 로 닉네임 / 프로필 이미지 URL 을 저장한다.
 *
 * 프로필 사진은 파일 업로드 방식(volume vs 오브젝트 스토리지)이 아직 미정이라,
 * 이번 단계에서는 이미지 URL 을 텍스트로 입력받아 저장하는 수준까지만 구현한다.
 */
export default function AccountSection() {
  const { user } = useAuth();
  const [nickname, setNickname] = useState(user?.nickname ?? '');
  const [profileImageUrl, setProfileImageUrl] = useState(user?.profileImageUrl ?? '');
  const [imgFailed, setImgFailed] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null); // { type:'ok'|'err', text }

  async function handleSave(e) {
    e.preventDefault();
    setMessage(null);
    if (!nickname.trim()) {
      setMessage({ type: 'err', text: '닉네임을 입력해주세요.' });
      return;
    }
    setSaving(true);
    try {
      // 빈 문자열로 보내면 이미지를 지울 수 있다(백엔드는 null 을 "미변경"으로 취급하므로
      // null 대신 "" 를 보낸다). 값이 있으면 그 URL 로 설정.
      const updated = await updateProfile({
        nickname: nickname.trim(),
        profileImageUrl: profileImageUrl.trim(),
      });
      setUser(updated); // 헤더 등 다른 화면에도 즉시 반영
      setImgFailed(false);
      setMessage({ type: 'ok', text: '저장되었습니다.' });
    } catch (err) {
      setMessage({ type: 'err', text: err.message });
    } finally {
      setSaving(false);
    }
  }

  const initial = (nickname || user?.loginId || '?').trim().charAt(0);

  return (
    <section>
      <h2 className="mb-4 text-xl font-bold text-dark">계정 관리</h2>

      <form
        onSubmit={handleSave}
        className="max-w-md space-y-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
      >
        {/* 프로필 미리보기 */}
        <div className="flex items-center gap-4">
          <span className="flex h-16 w-16 items-center justify-center overflow-hidden rounded-full border-2 border-primary bg-white">
            {profileImageUrl && !imgFailed ? (
              <img
                src={profileImageUrl}
                alt="프로필"
                className="h-full w-full object-cover"
                onError={() => setImgFailed(true)}
              />
            ) : (
              <span className="text-2xl font-bold text-primary">{initial}</span>
            )}
          </span>
          <div className="text-sm">
            <p className="text-dark/60">
              아이디 <span className="font-semibold text-dark">{user?.loginId}</span>
            </p>
            <p className="text-dark/60">
              역할 <span className="font-semibold text-dark">{user?.role}</span>
            </p>
          </div>
        </div>

        <label className="block">
          <span className="mb-1 block text-sm font-medium text-dark/70">닉네임</span>
          <input
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-dark outline-none transition focus:border-primary focus:ring-1 focus:ring-primary"
          />
        </label>

        <label className="block">
          <span className="mb-1 block text-sm font-medium text-dark/70">
            프로필 이미지 URL <span className="text-dark/40">(파일 업로드는 추후 지원)</span>
          </span>
          <input
            type="text"
            value={profileImageUrl}
            onChange={(e) => {
              setProfileImageUrl(e.target.value);
              setImgFailed(false);
            }}
            placeholder="https://... 또는 /assets/imgs/..."
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-dark outline-none transition focus:border-primary focus:ring-1 focus:ring-primary"
          />
        </label>

        {message && (
          <p className={`text-sm ${message.type === 'ok' ? 'text-green-600' : 'text-red-500'}`}>
            {message.text}
          </p>
        )}

        <button
          type="submit"
          disabled={saving}
          className="rounded-lg bg-primary px-4 py-2 font-semibold text-white transition hover:bg-light disabled:opacity-50"
        >
          {saving ? '저장 중...' : '변경사항 저장'}
        </button>
      </form>
    </section>
  );
}
