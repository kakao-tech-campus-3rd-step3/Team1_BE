package knu.team1.be.boost.auth.service;

import io.jsonwebtoken.JwtException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.KakaoDto;
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

    @Transactional
    public TokenDto login(String code) {
        KakaoDto.UserInfo kakaoUserInfo = kakaoClientService.getUserInfo(code);
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
    public TokenDto reissue(String refreshToken) {
        // Refresh Token에서 memberId 추출 및 자체의 유효성 검증
        UUID userId;
        try {
            userId = jwtUtil.getUserId(refreshToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken storedRefreshToken = refreshTokenRepository.findByMemberId(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.REFRESH_TOKEN_NOT_FOUND,
                "memberId: " + userId
            ));

        if (!storedRefreshToken.getRefreshToken().equals(refreshToken)) {
            throw new BusinessException(
                ErrorCode.REFRESH_TOKEN_NOT_EQUALS,
                "refreshToken mismatch for memberId: " + userId
            );
        }

        // 모든 검증을 통과하면 새로운 토큰을 생성
        Member member = storedRefreshToken.getMember();
        if (member == null) {
            throw new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + userId
            );
        }
        Authentication userAuthentication = createUserAuthentication(member);
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
