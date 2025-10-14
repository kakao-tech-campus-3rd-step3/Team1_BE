package knu.team1.be.boost.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.LoginDto;
import knu.team1.be.boost.auth.dto.LoginRequestDto;
import knu.team1.be.boost.auth.dto.TokenDto;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.auth.service.AuthService;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.security.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("카카오 로그인 API")
    class KakaoLogin {

        @Test
        @DisplayName("성공")
        void kakaoLogin_Success() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("test_kakao_auth_code");
            TokenDto tokenDto = new TokenDto("mock_access_token", "mock_refresh_token");
            MemberResponseDto memberResponseDto = new MemberResponseDto(
                UUID.randomUUID(),
                "수정된 이름",
                "1112",
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            LoginDto loginDto = LoginDto.of(memberResponseDto, tokenDto, true);
            given(authService.login(requestDto.code())).willReturn(loginDto);

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/login/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

            // then
            resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessTokenResponseDto.accessToken").value(
                    loginDto.tokenDto().accessToken()))
                .andExpect(cookie().exists("refreshToken"));
        }

        @Test
        @DisplayName("실패: 인가 코드가 없을 경우 MissingServletRequestParameterException")
        void kakaoLogin_Fail_WhenCodeIsMissing() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/login/kakao"));
            // then
            resultActions.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 인가 코드일 경우 KAKAO_INVALID_AUTH_CODE")
        void kakaoLogin_Fail_WhenServiceThrowsBusinessException() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("invalid_kakao_code");
            given(authService.login(requestDto.code()))
                .willThrow(new BusinessException(ErrorCode.KAKAO_INVALID_AUTH_CODE));

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/login/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

            // then
            resultActions.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class Logout {

        @Test
        @DisplayName("성공")
        void logout_Success() throws Exception {
            // given
            UserPrincipalDto principal = new UserPrincipalDto(
                UUID.randomUUID(),
                "testUser",
                "1111"
            );
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));
            Authentication testAuthentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
            );
            doNothing().when(authService).logout(any(UserPrincipalDto.class));

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/logout")
                .with(authentication(testAuthentication)));

            // then
            resultActions
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refreshToken", 0));
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자의 요청일 경우 AUTHENTICATION_FAILED")
        void logout_Fail_WhenNotAuthenticated() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/logout")
                .with(anonymous()));
            // then
            resultActions.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 API")
    class Reissue {

        @Test
        @DisplayName("성공")
        void reissue_Success() throws Exception {
            // given
            String validRefreshToken = "valid_refresh_token";
            TokenDto newTokenDto = new TokenDto("new_access_token", "new_refresh_token");

            given(authService.reissue(validRefreshToken))
                .willReturn(newTokenDto);

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/reissue")
                .cookie(new Cookie("refreshToken", validRefreshToken)));

            // then
            resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newTokenDto.accessToken()))
                .andExpect(cookie().value("refreshToken", newTokenDto.refreshToken()));
        }

        @Test
        @DisplayName("실패: Refresh Token 쿠키가 없을 경우 MissingRequestCookieException")
        void reissue_Fail_WhenCookieIsMissing() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/reissue")
                .header(HttpHeaders.AUTHORIZATION, "Bearer expired_token"));
            // then
            resultActions.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 Refresh Token일 경우 INVALID_REFRESH_TOKEN")
        void reissue_Fail_WhenRefreshTokenIsInvalid() throws Exception {
            // given
            String invalidRefreshToken = "invalid_token";
            given(authService.reissue(invalidRefreshToken))
                .willThrow(new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/reissue")
                .cookie(new Cookie("refreshToken", invalidRefreshToken)));

            // then
            resultActions.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패: DB에 Refresh Token이 없을 경우 REFRESH_TOKEN_NOT_FOUND")
        void reissue_Fail_WhenRefreshTokenNotFound() throws Exception {
            // given
            String unknownRefreshToken = "unknown_token";
            given(authService.reissue(unknownRefreshToken))
                .willThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

            // when
            ResultActions resultActions = mockMvc.perform(post("/api/auth/reissue")
                .cookie(new Cookie("refreshToken", unknownRefreshToken)));

            // then
            resultActions.andExpect(status().isNotFound());
        }
    }
}

