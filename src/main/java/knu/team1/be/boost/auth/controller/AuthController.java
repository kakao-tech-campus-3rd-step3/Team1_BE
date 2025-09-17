package knu.team1.be.boost.auth.controller;

import knu.team1.be.boost.auth.dto.AccessTokenResponseDto;
import knu.team1.be.boost.auth.dto.TokenDto;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.auth.service.AuthService;
import knu.team1.be.boost.common.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    private final JwtTokenProvider jwtTokenProvider;

    private static final long REFRESH_TOKEN_EXPIRE_TIME_SECONDS = 7 * 24 * 60 * 60; // 7일

    @Override
    public ResponseEntity<AccessTokenResponseDto> kakaoLogin(@RequestParam("code") String code) {
        TokenDto tokenDto = authService.login(code);

        // 헤더에 Refresh Token 쿠키 추가
        HttpHeaders headers = createCookieHeaders(tokenDto.refreshToken());

        AccessTokenResponseDto accessToken = AccessTokenResponseDto.from(tokenDto.accessToken());

        return ResponseEntity.ok().headers(headers).body(accessToken);
    }

    @Override
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipalDto userPrincipalDto) {
        authService.logout(userPrincipalDto);

        // 쿠키를 만료시키는 헤더를 생성
        HttpHeaders headers = createExpiredCookieHeaders();

        return ResponseEntity.ok().headers(headers).build();
    }

    @Override
    public ResponseEntity<AccessTokenResponseDto> reissue(
        @CookieValue("refreshToken") String refreshToken,
        @RequestHeader("Authorization") String accessTokenHeader
    ) {
        String expiredAccessToken = jwtTokenProvider.resolveToken(accessTokenHeader);
        TokenDto tokenDto = authService.reissue(expiredAccessToken, refreshToken);

        HttpHeaders headers = createCookieHeaders(tokenDto.refreshToken());

        AccessTokenResponseDto newAccessToken = AccessTokenResponseDto.from(tokenDto.accessToken());

        return ResponseEntity.ok().headers(headers).body(newAccessToken);
    }

    private HttpHeaders createCookieHeaders(String refreshTokenValue) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshTokenValue)
            .maxAge(REFRESH_TOKEN_EXPIRE_TIME_SECONDS)
            .path("/")
            .secure(true)
            .sameSite("Strict")
            .httpOnly(true)
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return headers;
    }

    private HttpHeaders createExpiredCookieHeaders() {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
            .maxAge(0)
            .path("/")
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return headers;
    }
}
