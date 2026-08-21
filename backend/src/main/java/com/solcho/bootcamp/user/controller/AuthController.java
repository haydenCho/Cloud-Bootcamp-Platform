package com.solcho.bootcamp.user.controller;

import com.solcho.bootcamp.common.dto.ApiResponse;
import com.solcho.bootcamp.config.JwtTokenProvider;
import com.solcho.bootcamp.config.UserPrincipal;
import com.solcho.bootcamp.user.dto.AuthResponse;
import com.solcho.bootcamp.user.dto.LoginRequest;
import com.solcho.bootcamp.user.dto.SignupRequest;
import com.solcho.bootcamp.user.dto.TokenResponse;
import com.solcho.bootcamp.user.service.AuthService;
import com.solcho.bootcamp.user.service.AuthTokens;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;
    private final long refreshMaxAgeSeconds;
    private final boolean cookieSecure;
    private final String cookieSameSite;
    private final String cookieDomain;

    public AuthController(
            AuthService authService,
            JwtTokenProvider tokenProvider,
            @org.springframework.beans.factory.annotation.Value("${app.cookie.secure:false}") boolean cookieSecure,
            @org.springframework.beans.factory.annotation.Value("${app.cookie.same-site:Lax}") String cookieSameSite,
            @org.springframework.beans.factory.annotation.Value("${app.cookie.domain:}") String cookieDomain) {
        this.authService = authService;
        this.refreshMaxAgeSeconds = tokenProvider.getRefreshTokenValiditySeconds();
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
        this.cookieDomain = cookieDomain;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest req) {
        AuthTokens tokens = authService.signup(req);
        return authResponse(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthTokens tokens = authService.login(req);
        return authResponse(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        AuthTokens tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken(), refreshMaxAgeSeconds).toString())
                .body(ApiResponse.ok(new TokenResponse(tokens.accessToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            authService.logout(principal.id());
        }
        // maxAge=0 쿠키로 클라이언트 refresh 쿠키 삭제
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie("", 0).toString())
                .body(ApiResponse.ok());
    }

    private ResponseEntity<ApiResponse<AuthResponse>> authResponse(AuthTokens tokens) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken(), refreshMaxAgeSeconds).toString())
                .body(ApiResponse.ok(AuthResponse.of(tokens.accessToken(), tokens.user())));
    }

    /**
     * refresh 토큰용 httpOnly 쿠키.
     * secure / SameSite / domain 은 환경변수로 주입한다(도메인·프로토콜 의존 값).
     * - 로컬 개발 & lib.solcho.com HTTP(동일 출처, nginx /api 프록시): secure=false, SameSite=Lax, domain 미설정(host-only).
     * - 이후 HTTPS(Let's Encrypt) 전환 시 COOKIE_SECURE=true 로만 바꾸면 된다(코드 변경 불필요).
     */
    private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite)
                .maxAge(maxAgeSeconds);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }
}
