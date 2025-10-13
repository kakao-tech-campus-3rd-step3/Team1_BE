package knu.team1.be.boost.auth.service;

import io.jsonwebtoken.JwtException;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import knu.team1.be.boost.auth.dto.KakaoDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtil jwtUtil;

    private final KakaoClientService kakaoClientService;

    private final String DEFAULT_AVATAR = "1111";

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String allowedRedirectUrisRaw;

    private Set<String> allowedRedirectUris;

    @PostConstruct
    private void init() {
        allowedRedirectUris = Arrays.stream(allowedRedirectUrisRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    }

    @Transactional
    public TokenDto login(LoginRequestDto requestDto) {
        if (!allowedRedirectUris.contains(requestDto.redirectUri())) {
            throw new BusinessException(
                ErrorCode.INVALID_REDIRECT_URI, "redirectUri: " + requestDto.redirectUri()
            );
        }

        KakaoDto.UserInfo kakaoUserInfo = kakaoClientService.getUserInfo(requestDto);
        Member member = registerOrLogin(kakaoUserInfo);

        Authentication userAuthentication = createUserAuthentication(member);
        TokenDto tokenDto = jwtUtil.generateToken(userAuthentication);

        saveOrUpdateRefreshToken(member, tokenDto.refreshToken());

        return tokenDto;
    }

    @Transactional
    public void logout(UserPrincipalDto userPrincipalDto) {
        refreshTokenRepository.deleteByMemberId(userPrincipalDto.id());
    }

    @Transactional
    public TokenDto reissue(String expiredAccessToken, String refreshToken) {
        // Refresh Token 자체의 유효성 검증
        try {
            jwtUtil.validateToken(refreshToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 만료된 Access Token에서 memberId 추출
        Authentication userAuthentication = jwtUtil.getAuthentication(expiredAccessToken);
        UserPrincipalDto userPrincipalDto = (UserPrincipalDto) userAuthentication.getPrincipal();

        RefreshToken storedRefreshToken = refreshTokenRepository.findByMemberId(
                userPrincipalDto.id())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.REFRESH_TOKEN_NOT_FOUND,
                "memberId: " + userPrincipalDto.id()
            ));

        if (!storedRefreshToken.getRefreshToken().equals(refreshToken)) {
            throw new BusinessException(
                ErrorCode.REFRESH_TOKEN_NOT_EQUALS,
                "refreshToken mismatch for memberId: " + userPrincipalDto.id()
            );
        }

        // 모든 검증을 통과하면 새로운 토큰을 생성
        TokenDto newTokenDto = jwtUtil.generateToken(userAuthentication);

        storedRefreshToken.updateToken(newTokenDto.refreshToken());

        return newTokenDto;
    }

    private Member registerOrLogin(KakaoDto.UserInfo userInfo) {
        return memberRepository.findByOauthInfoProviderAndOauthInfoProviderId(
            "kakao", userInfo.id()
        ).orElseGet(() -> {
            OauthInfo oauthInfo = OauthInfo.builder()
                .provider("kakao")
                .providerId(userInfo.id())
                .build();
            Member newMember = Member.builder()
                .name(userInfo.kakaoAccount()
                    .profile()
                    .nickname()
                )
                .avatar(DEFAULT_AVATAR)
                .oauthInfo(oauthInfo)
                .build();
            return memberRepository.save(newMember);
        });
    }

    private Authentication createUserAuthentication(Member member) {
        UserPrincipalDto userPrincipalDto = UserPrincipalDto.from(
            member.getId(),
            member.getName(),
            member.getAvatar()
        );

        // 권한 정보 생성 (기본 USER 권한)
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));

        return new UsernamePasswordAuthenticationToken(userPrincipalDto, null, authorities);
    }

    private void saveOrUpdateRefreshToken(Member member, String tokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(member.getId())
            .orElse(RefreshToken.builder()
                .member(member)
                .build()
            );

        refreshToken.updateToken(tokenValue);
        refreshTokenRepository.save(refreshToken);
    }
}
