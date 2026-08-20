package com.solcho.bootcamp.config;

import com.solcho.bootcamp.user.entity.Role;

/**
 * SecurityContext 에 저장되는 인증 주체. 컨트롤러에서 @AuthenticationPrincipal 로 주입받는다.
 */
public record UserPrincipal(Long id, String loginId, Role role) {
}
