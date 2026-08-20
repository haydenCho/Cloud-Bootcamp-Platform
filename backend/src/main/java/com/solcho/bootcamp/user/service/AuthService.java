package com.solcho.bootcamp.user.service;

import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.config.JwtTokenProvider;
import com.solcho.bootcamp.user.dto.LoginRequest;
import com.solcho.bootcamp.user.dto.SignupRequest;
import com.solcho.bootcamp.user.entity.Role;
import com.solcho.bootcamp.user.entity.User;
import com.solcho.bootcamp.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 / 로그인 / 토큰 재발급 / 로그아웃.
 * Refresh Token 원문은 클라이언트 httpOnly 쿠키에만 두고, 서버에는 해시만 저장한다.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthTokens signup(SignupRequest req) {
        if (userRepository.existsByLoginId(req.loginId())) {
            throw ApiException.conflict("이미 사용 중인 아이디입니다.");
        }
        User user = User.builder()
                .loginId(req.loginId())
                .passwordHash(passwordEncoder.encode(req.password()))
                .nickname(req.nickname())
                .role(Role.USER)
                .build();
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthTokens login(LoginRequest req) {
        User user = userRepository.findByLoginId(req.loginId())
                .orElseThrow(() -> ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return issueTokens(user);
    }

    /** Refresh 쿠키 → 새 Access Token 발급 (Refresh Token 도 회전). */
    @Transactional
    public AuthTokens refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw ApiException.unauthorized("리프레시 토큰이 없습니다.");
        }
        var claims = tokenProvider.parse(refreshToken);
        if (claims == null || !"refresh".equals(claims.get("type", String.class))) {
            throw ApiException.unauthorized("유효하지 않은 리프레시 토큰입니다.");
        }
        User user = userRepository.findById(tokenProvider.getUserId(claims))
                .orElseThrow(() -> ApiException.unauthorized("유효하지 않은 리프레시 토큰입니다."));

        if (user.getRefreshTokenHash() == null
                || user.getRefreshTokenExpiresAt() == null
                || user.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())
                || !constantTimeEquals(sha256(refreshToken), user.getRefreshTokenHash())) {
            throw ApiException.unauthorized("만료되었거나 무효화된 리프레시 토큰입니다.");
        }
        return issueTokens(user);
    }

    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(User::clearRefreshToken);
    }

    /** Access + Refresh 토큰을 만들고, Refresh 해시를 사용자에 저장한다. */
    private AuthTokens issueTokens(User user) {
        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = tokenProvider.createRefreshToken(user.getId());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(tokenProvider.getRefreshTokenValiditySeconds());
        // Refresh Token 은 고엔트로피 무작위 값이라 BCrypt(느린 해시, 72바이트 제한) 대신
        // SHA-256 으로 해시해 저장한다. 원문은 클라이언트 httpOnly 쿠키에만 존재한다.
        user.updateRefreshToken(sha256(refreshToken), expiresAt);
        return new AuthTokens(accessToken, refreshToken, user);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 미지원 환경", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
