package knu.team1.be.boost.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.KakaoDto;
import knu.team1.be.boost.auth.dto.LoginDto;
import knu.team1.be.boost.auth.dto.LoginRequestDto;
import knu.team1.be.boost.auth.dto.TokenDto;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.auth.entity.RefreshToken;
import knu.team1.be.boost.auth.repository.RefreshTokenRepository;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.entity.vo.OauthInfo;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private KakaoClientService kakaoClientService;

    private final String DEFAULT_AVATAR = "1111";

    @Nested
    @DisplayName("로그인/회원가입")
    class Login {

        @Test
        @DisplayName("성공: 신규 사용자일 경우 회원가입 후 토큰 발급")
        void login_Success_WhenNewUser() {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("test_code", "test_redirect_uri");
            KakaoDto.UserInfo mockKakaoUser = createMockKakaoUser(12345L, "라이언");
            Member newMember = createMockMember(
                UUID.randomUUID(),
                mockKakaoUser.id(),
                mockKakaoUser.kakaoAccount()
                    .profile()
                    .nickname()
            );
            TokenDto mockTokenDto = new TokenDto("access", "refresh");

            given(kakaoClientService.getUserInfo(requestDto)).willReturn(mockKakaoUser);
            given(memberRepository.findByOauthInfoProviderAndOauthInfoProviderId("kakao",
                mockKakaoUser.id())).willReturn(Optional.empty());
            given(memberRepository.save(any(Member.class))).willReturn(newMember);
            given(jwtUtil.generateToken(any(Authentication.class))).willReturn(mockTokenDto);

            // when
            LoginDto resultLoginDto = authService.login(requestDto);

            // then
            assertThat(resultLoginDto.tokenDto()).isEqualTo(mockTokenDto);
            verify(memberRepository).save(any(Member.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("성공: 기존 사용자일 경우 로그인 후 토큰 발급")
        void login_Success_WhenExistingUser() {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("test_code", "test_redirect_uri");
            KakaoDto.UserInfo mockKakaoUser = createMockKakaoUser(12345L, "라이언");
            Member existingMember = createMockMember(
                UUID.randomUUID(),
                mockKakaoUser.id(),
                mockKakaoUser.kakaoAccount()
                    .profile()
                    .nickname()
            );
            TokenDto mockTokenDto = new TokenDto("access", "refresh");

            given(kakaoClientService.getUserInfo(requestDto)).willReturn(mockKakaoUser);
            given(memberRepository.findByOauthInfoProviderAndOauthInfoProviderId("kakao",
                mockKakaoUser.id())).willReturn(Optional.of(existingMember));
            given(jwtUtil.generateToken(any(Authentication.class))).willReturn(mockTokenDto);

            // when
            LoginDto resultLoginDto = authService.login(requestDto);

            // then
            assertThat(resultLoginDto.tokenDto()).isEqualTo(mockTokenDto);
            verify(memberRepository, never()).save(any(Member.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공")
        void logout_Success() {
            // given
            UUID memberId = UUID.randomUUID();
            UserPrincipalDto userPrincipalDto = new UserPrincipalDto(
                memberId,
                "testUser",
                DEFAULT_AVATAR
            );
            doNothing().when(refreshTokenRepository)
                .deleteByMemberId(memberId);

            // when
            authService.logout(userPrincipalDto);

            // then
            verify(refreshTokenRepository).deleteByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class Reissue {

        private Authentication authentication;
        private UserPrincipalDto principal;

        @BeforeEach
        void setUp() {
            UUID memberId = UUID.randomUUID();
            principal = new UserPrincipalDto(
                memberId,
                "testUser",
                DEFAULT_AVATAR
            );
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));
            authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
            );
        }

        @Test
        @DisplayName("성공")
        void reissue_Success() {
            // given
            String validRefreshToken = "valid_refresh";
            Member member = createMockMember(
                principal.id(),
                12345L,
                "testUser"
            );
            RefreshToken storedToken = RefreshToken.builder()
                .member(member)
                .refreshToken(validRefreshToken)
                .build();
            TokenDto newTokenDto = new TokenDto("new_access", "new_refresh");

            given(jwtUtil.getUserId(validRefreshToken)).willReturn(member.getId());
            given(refreshTokenRepository.findByMemberId(principal.id()))
                .willReturn(Optional.of(storedToken));
            given(jwtUtil.generateToken(any(Authentication.class))).willReturn(newTokenDto);

            // when
            TokenDto resultTokenDto = authService.reissue(validRefreshToken);

            // then
            assertThat(resultTokenDto).isEqualTo(newTokenDto);
            assertThat(storedToken.getRefreshToken()).isEqualTo(newTokenDto.refreshToken());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 리프레시 토큰일 경우 INVALID_REFRESH_TOKEN")
        void reissue_Fail_WhenRefreshTokenIsInvalid() {
            // given
            String invalidRefreshToken = "invalid_refresh";
            given(jwtUtil.getUserId(invalidRefreshToken))
                .willThrow(new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

            // when & then
            assertThatThrownBy(() -> authService.reissue(invalidRefreshToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("실패: DB에 리프레시 토큰이 없을 경우 REFRESH_TOKEN_NOT_FOUND")
        void reissue_Fail_WhenRefreshTokenNotFound() {
            // given
            String unknownRefreshToken = "unknown_refresh";

            given(jwtUtil.getUserId(unknownRefreshToken))
                .willReturn(principal.id());
            given(refreshTokenRepository.findByMemberId(principal.id()))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue(unknownRefreshToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 요청된 리프레시 토큰과 DB의 토큰이 다를 경우 REFRESH_TOKEN_NOT_EQUALS")
        void reissue_Fail_WhenRefreshTokenMismatched() {
            // given
            String clientRefreshToken = "client_refresh";
            String storedRefreshTokenValue = "stored_refresh";
            Member member = createMockMember(
                principal.id(),
                12345L,
                "testUser"
            );
            RefreshToken storedToken = RefreshToken.builder().member(member)
                .refreshToken(storedRefreshTokenValue).build();

            given(jwtUtil.getUserId(clientRefreshToken))
                .willReturn(principal.id());
            given(refreshTokenRepository.findByMemberId(principal.id()))
                .willReturn(Optional.of(storedToken));

            // when & then
            assertThatThrownBy(() -> authService.reissue(clientRefreshToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_NOT_EQUALS);
        }
    }

    private KakaoDto.UserInfo createMockKakaoUser(Long id, String nickname) {
        return new KakaoDto.UserInfo(id, new KakaoDto.UserInfo.KakaoAccount(
            new KakaoDto.UserInfo.KakaoAccount.Profile(nickname))
        );
    }

    private Member createMockMember(
        UUID id,
        Long providerId,
        String name
    ) {
        return Member.builder()
            .id(id)
            .oauthInfo(OauthInfo.builder()
                .provider("kakao")
                .providerId(providerId)
                .build()
            ).name(name)
            .avatar(DEFAULT_AVATAR)
            .build();
    }
}

