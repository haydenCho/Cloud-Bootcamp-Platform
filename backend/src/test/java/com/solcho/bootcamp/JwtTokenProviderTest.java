package com.solcho.bootcamp;

import static org.assertj.core.api.Assertions.assertThat;

import com.solcho.bootcamp.config.JwtProperties;
import com.solcho.bootcamp.config.JwtTokenProvider;
import com.solcho.bootcamp.user.entity.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

/** DB / Spring 컨텍스트 없이 도는 순수 단위 테스트. */
class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider(
            new JwtProperties("test-secret-key-that-is-long-enough-32bytes!", 30, 14));

    @Test
    void accessToken_은_userId_와_role_을_담고_검증된다() {
        String token = provider.createAccessToken(42L, Role.ADMIN);

        Claims claims = provider.parse(token);

        assertThat(claims).isNotNull();
        assertThat(provider.getUserId(claims)).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
    }

    @Test
    void refreshToken_은_type_이_refresh_이다() {
        String token = provider.createRefreshToken(7L);

        Claims claims = provider.parse(token);

        assertThat(claims).isNotNull();
        assertThat(provider.getUserId(claims)).isEqualTo(7L);
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
    }

    @Test
    void 변조된_토큰은_null_을_반환한다() {
        String token = provider.createAccessToken(1L, Role.USER);

        Claims claims = provider.parse(token + "tampered");

        assertThat(claims).isNull();
    }
}
