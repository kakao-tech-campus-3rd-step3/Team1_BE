package knu.team1.be.boost.auth.service;

import io.jsonwebtoken.JwtException;
import java.util.Collection;
import java.util.List;
import knu.team1.be.boost.auth.dto.KakaoDto;
import knu.team1.be.boost.auth.dto.TokenDto;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.auth.entity.RefreshToken;
import knu.team1.be.boost.auth.exception.InvalidRefreshTokenException;
import knu.team1.be.boost.auth.exception.RefreshTokenNotEqualsException;
import knu.team1.be.boost.auth.exception.RefreshTokenNotFoundException;
import knu.team1.be.boost.auth.repository.RefreshTokenRepository;
import knu.team1.be.boost.common.config.jwt.JwtTokenProvider;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.entity.vo.OauthInfo;
import knu.team1.be.boost.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
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

    private final JwtTokenProvider jwtTokenProvider;

    private final KakaoClientService kakaoClientService;

    @Transactional
    public TokenDto login(String code) {
        KakaoDto.UserInfo kakaoUserInfo = kakaoClientService.getUserInfo(code);
        Member member = registerOrLogin(kakaoUserInfo);

        Authentication userAuthentication = createUserAuthentication(member);
        TokenDto tokenDto = jwtTokenProvider.generateToken(userAuthentication);

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
            jwtTokenProvider.validateToken(refreshToken);
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }

        // 만료된 Access Token에서 memberId 추출
        Authentication userAuthentication = jwtTokenProvider.getAuthentication(expiredAccessToken);
        UserPrincipalDto userPrincipalDto = (UserPrincipalDto) userAuthentication.getPrincipal();

        RefreshToken storedRefreshToken = refreshTokenRepository.findByMemberId(
                userPrincipalDto.id())
            .orElseThrow(RefreshTokenNotFoundException::new);

        if (!storedRefreshToken.getRefreshToken().equals(refreshToken)) {
            throw new RefreshTokenNotEqualsException();
        }

        // 모든 검증을 통과하면 새로운 토큰을 생성
        TokenDto newTokenDto = jwtTokenProvider.generateToken(userAuthentication);

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
