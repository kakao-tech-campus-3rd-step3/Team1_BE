package knu.team1.be.boost.auth.controller;

import java.time.Duration;
import knu.team1.be.boost.auth.dto.AccessTokenResponseDto;
import knu.team1.be.boost.auth.dto.LoginRequestDto;
import knu.team1.be.boost.auth.dto.TokenDto;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.auth.service.AuthService;
import knu.team1.be.boost.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expire-time}")
    private Duration refreshTokenExpireTime;

    @Override
    public ResponseEntity<AccessTokenResponseDto> kakaoLogin(
        @RequestBody LoginRequestDto requestDto
    ) {
        TokenDto tokenDto = authService.login(requestDto.code());

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

        return ResponseEntity.noContent().headers(headers).build();
    }

    @Override
    public ResponseEntity<AccessTokenResponseDto> reissue(
        @CookieValue("refreshToken") String refreshToken
    ) {
        TokenDto tokenDto = authService.reissue(refreshToken);

        HttpHeaders headers = createCookieHeaders(tokenDto.refreshToken());

        AccessTokenResponseDto newAccessToken = AccessTokenResponseDto.from(tokenDto.accessToken());

        return ResponseEntity.ok().headers(headers).body(newAccessToken);
    }

    private HttpHeaders createCookieHeaders(String refreshTokenValue) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshTokenValue)
            .maxAge(refreshTokenExpireTime)
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
            .secure(true)
            .sameSite("Strict")
            .httpOnly(true)
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return headers;
    }
}
