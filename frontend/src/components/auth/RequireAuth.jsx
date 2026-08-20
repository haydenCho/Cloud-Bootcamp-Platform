import { useAuth } from '../../store/authStore';
import LoginPrompt from './LoginPrompt';

/**
 * 인증이 필요한 화면을 감싸는 래퍼.
 * 비로그인 시 라우트를 막거나 튕기지 않고 LoginPrompt 를 렌더링한다(로그인 후 원래 경로 복귀).
 *
 * 사용: <RequireAuth><CommunityListPage /></RequireAuth>
 */
export default function RequireAuth({ children, description }) {
  const { user } = useAuth();
  if (!user) {
    return <LoginPrompt description={description} />;
  }
  return children;
}
