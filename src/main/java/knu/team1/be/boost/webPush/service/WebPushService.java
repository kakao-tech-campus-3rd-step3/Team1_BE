package knu.team1.be.boost.webPush.service;

import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.webPush.dto.WebPushConnectDto;
import knu.team1.be.boost.webPush.dto.WebPushRegisterDto;
import knu.team1.be.boost.webPush.dto.WebPushSession;
import knu.team1.be.boost.webPush.dto.WebPushSessionResponseDto;
import knu.team1.be.boost.webPush.dto.WebPushSessionStatus;
import knu.team1.be.boost.webPush.entity.WebPushSubscription;
import knu.team1.be.boost.webPush.repository.WebPushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebPushService {

    private final WebPushSessionCacheService cacheService;
    private final WebPushRepository webPushRepository;
    private final MemberRepository memberRepository;

    public WebPushSessionResponseDto createSession(UserPrincipalDto user) {
        String token = UUID.randomUUID().toString();

        WebPushSession session = WebPushSession.builder()
            .token(token)
            .userId(user.id())
            .status(WebPushSessionStatus.CREATED)
            .build();

        cacheService.saveSession(session);
        return WebPushSessionResponseDto.from(token, session.status());
    }

    public WebPushSessionResponseDto connectDevice(WebPushConnectDto dto) {
        WebPushSession session = validateAndGetSession(dto.token());

        if (session.status() != WebPushSessionStatus.CREATED) {
            throw new BusinessException(
                ErrorCode.INVALID_WEB_PUSH_STATE_TRANSITION,
                "memberId: " + session.userId() + " current status: " + session.status()
            );
        }

        WebPushSession updated = WebPushSession.builder()
            .token(dto.token())
            .userId(session.userId())
            .status(WebPushSessionStatus.CONNECTED)
            .deviceInfo(dto.deviceInfo())
            .build();

        cacheService.saveSession(updated);

        return WebPushSessionResponseDto.from(updated.token(), updated.status());
    }

    public WebPushSessionResponseDto getSessionStatus(String token) {
        WebPushSession session = validateAndGetSession(token);
        return WebPushSessionResponseDto.from(session.token(), session.status());
    }

    @Transactional
    public WebPushSessionResponseDto registerSubscription(WebPushRegisterDto registerDto) {
        WebPushSession session = validateAndGetSession(registerDto.token());

        WebPushSession updated = WebPushSession.builder()
            .token(registerDto.token())
            .userId(session.userId())
            .status(WebPushSessionStatus.REGISTERED)
            .deviceInfo(session.deviceInfo())
            .webPushUrl(registerDto.webPushUrl())
            .publicKey(registerDto.publicKey())
            .authKey(registerDto.authKey())
            .build();

        cacheService.saveSession(updated);

        Member member = memberRepository.findById(session.userId())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND, "memberId: " + session.userId()
            ));

        WebPushSubscription existing = webPushRepository
            .findByMemberIdAndDeviceInfo(member.getId(), session.deviceInfo())
            .orElse(null);

        if (existing != null) {
            existing.updateSubscription(
                registerDto.webPushUrl(),
                registerDto.publicKey(),
                registerDto.authKey()
            );
        } else {
            WebPushSubscription subscription = WebPushSubscription.builder()
                .token(registerDto.token())
                .deviceInfo(session.deviceInfo())
                .webPushUrl(registerDto.webPushUrl())
                .publicKey(registerDto.publicKey())
                .authKey(registerDto.authKey())
                .member(member)
                .build();

            webPushRepository.save(subscription);
        }

        return WebPushSessionResponseDto.from(updated.token(), updated.status());
    }

    private WebPushSession validateAndGetSession(String token) {
        WebPushSession session = cacheService.getSession(token);
        if (session == null) {
            throw new BusinessException(ErrorCode.INVALID_WEB_PUSH_TOKEN);
        }
        return session;
    }
}
