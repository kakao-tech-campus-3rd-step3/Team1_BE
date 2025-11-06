package knu.team1.be.boost.webPush.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebPushServiceTest {

    @Mock
    WebPushSessionCacheService cacheService;
    @Mock
    WebPushRepository webPushRepository;
    @Mock
    MemberRepository memberRepository;

    WebPushService webPushService;

    UUID userId;
    UserPrincipalDto user;

    @BeforeEach
    void setUp() {
        webPushService = new WebPushService(cacheService, webPushRepository, memberRepository);
        userId = UUID.randomUUID();
        user = UserPrincipalDto.from(userId, "테스트유저", "avatar");
    }

    @Nested
    @DisplayName("웹푸시 세션 생성")
    class CreateSession {

        @Test
        @DisplayName("웹푸시 세션 생성 성공 - CREATED 상태로 세션 생성 및 캐시에 저장")
        void success() {
            // when
            WebPushSessionResponseDto res = webPushService.createSession(user);

            // then
            assertThat(res).isNotNull();
            assertThat(res.status()).isEqualTo(WebPushSessionStatus.CREATED);

            ArgumentCaptor<WebPushSession> captor = ArgumentCaptor.forClass(WebPushSession.class);
            verify(cacheService).saveSession(captor.capture());
            WebPushSession saved = captor.getValue();
            assertThat(saved.userId()).isEqualTo(userId);
            assertThat(saved.status()).isEqualTo(WebPushSessionStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("디바이스 연결")
    class ConnectDevice {

        @Test
        @DisplayName("디바이스 연결 성공 - CREATED 상태에서 CONNECTED로 변경")
        void success() {
            String token = UUID.randomUUID().toString();
            WebPushSession existing = Fixtures.session(token, userId, WebPushSessionStatus.CREATED,
                null);

            given(cacheService.getSession(token)).willReturn(existing);

            WebPushConnectDto req = new WebPushConnectDto(token, "device-info-1");

            WebPushSessionResponseDto res = webPushService.connectDevice(req);

            assertThat(res.status()).isEqualTo(WebPushSessionStatus.CONNECTED);
            verify(cacheService).saveSession(any(WebPushSession.class));
        }

        @Test
        @DisplayName("디바이스 연결 실패 - 존재하지 않는 세션 토큰")
        void fail_invalidToken() {
            String token = "invalid-token";
            given(cacheService.getSession(token)).willReturn(null);

            WebPushConnectDto req = new WebPushConnectDto(token, "device-info");

            assertThatThrownBy(() -> webPushService.connectDevice(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_WEB_PUSH_TOKEN);
        }

        @Test
        @DisplayName("디바이스 연결 실패 - 이미 CONNECTED 상태에서 재연결 시도")
        void fail_invalidTransition() {
            String token = UUID.randomUUID().toString();
            WebPushSession existing = Fixtures.session(token, userId,
                WebPushSessionStatus.CONNECTED, "device1");

            given(cacheService.getSession(token)).willReturn(existing);

            WebPushConnectDto req = new WebPushConnectDto(token, "device1");

            assertThatThrownBy(() -> webPushService.connectDevice(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.INVALID_WEB_PUSH_STATE_TRANSITION);
        }
    }

    @Nested
    @DisplayName("세션 상태 조회")
    class GetSessionStatus {

        @Test
        @DisplayName("세션 상태 조회 성공 - 캐시에서 세션을 정상적으로 조회")
        void success() {
            String token = UUID.randomUUID().toString();
            WebPushSession existing = Fixtures.session(token, userId,
                WebPushSessionStatus.CONNECTED, "deviceX");
            given(cacheService.getSession(token)).willReturn(existing);

            WebPushSessionResponseDto res = webPushService.getSessionStatus(token);

            assertThat(res.token()).isEqualTo(token);
            assertThat(res.status()).isEqualTo(WebPushSessionStatus.CONNECTED);
        }

        @Test
        @DisplayName("세션 상태 조회 실패 - 유효하지 않은 토큰 (세션 없음)")
        void fail_invalidToken() {
            String token = "no-session";
            given(cacheService.getSession(token)).willReturn(null);

            assertThatThrownBy(() -> webPushService.getSessionStatus(token))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_WEB_PUSH_TOKEN);
        }
    }

    @Nested
    @DisplayName("푸시 구독 등록")
    class RegisterSubscription {

        @Test
        @DisplayName("푸시 구독 등록 성공 - CONNECTED 상태에서 REGISTERED로 변경 후 저장")
        void success() {
            String token = UUID.randomUUID().toString();
            WebPushSession existing = Fixtures.session(token, userId,
                WebPushSessionStatus.CONNECTED, "device-1");
            given(cacheService.getSession(token)).willReturn(existing);

            Member member = Fixtures.member(userId, "테스트유저");
            given(memberRepository.findById(userId)).willReturn(Optional.of(member));
            given(webPushRepository.findByMemberIdAndDeviceInfo(userId, "device-1"))
                .willReturn(Optional.empty());
            given(webPushRepository.save(any(WebPushSubscription.class)))
                .willAnswer(inv -> inv.getArgument(0));

            WebPushRegisterDto req = Fixtures.reqRegister(token);

            WebPushSessionResponseDto res = webPushService.registerSubscription(req);

            assertThat(res.status()).isEqualTo(WebPushSessionStatus.REGISTERED);
            verify(cacheService).saveSession(any(WebPushSession.class));
            verify(webPushRepository).save(any(WebPushSubscription.class));
        }

        @Test
        @DisplayName("푸시 구독 등록 성공 - 이미 등록된 디바이스면 updateSubscription 호출")
        void success_updateExistingSubscription() {
            String token = UUID.randomUUID().toString();
            WebPushSession existing = Fixtures.session(token, userId,
                WebPushSessionStatus.CONNECTED, "device-1");
            given(cacheService.getSession(token)).willReturn(existing);

            Member member = Fixtures.member(userId, "비버");
            given(memberRepository.findById(userId)).willReturn(Optional.of(member));

            WebPushSubscription subscription = mock(WebPushSubscription.class);
            given(webPushRepository.findByMemberIdAndDeviceInfo(userId, "device-1"))
                .willReturn(Optional.of(subscription));

            WebPushRegisterDto req = Fixtures.reqRegister(token);

            webPushService.registerSubscription(req);

            verify(subscription).updateSubscription(
                eq(req.webPushUrl()), eq(req.publicKey()), eq(req.authKey())
            );
            verify(webPushRepository, never()).save(any());
        }

        @Test
        @DisplayName("푸시 구독 등록 실패 - 세션이 없음")
        void fail_noSession() {
            String token = "missing-token";
            given(cacheService.getSession(token)).willReturn(null);

            WebPushRegisterDto req = Fixtures.reqRegister(token);
            assertThatThrownBy(() -> webPushService.registerSubscription(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_WEB_PUSH_TOKEN);
        }

        @Test
        @DisplayName("푸시 구독 등록 실패 - CREATED 상태에서 등록 시도")
        void fail_invalidTransition() {
            String token = UUID.randomUUID().toString();
            WebPushSession existing = Fixtures.session(token, userId, WebPushSessionStatus.CREATED,
                "device");
            given(cacheService.getSession(token)).willReturn(existing);

            WebPushRegisterDto req = Fixtures.reqRegister(token);

            assertThatThrownBy(() -> webPushService.registerSubscription(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                    ErrorCode.INVALID_WEB_PUSH_STATE_TRANSITION);
        }

        @Test
        @DisplayName("푸시 구독 등록 실패 - MEMBER_NOT_FOUND")
        void fail_memberNotFound() {
            String token = UUID.randomUUID().toString();
            WebPushSession existing = Fixtures.session(token, userId,
                WebPushSessionStatus.CONNECTED, "device");
            given(cacheService.getSession(token)).willReturn(existing);
            given(memberRepository.findById(userId)).willReturn(Optional.empty());

            WebPushRegisterDto req = Fixtures.reqRegister(token);

            assertThatThrownBy(() -> webPushService.registerSubscription(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    static class Fixtures {

        static WebPushSession session(String token, UUID userId, WebPushSessionStatus status,
            String device) {
            return WebPushSession.builder()
                .token(token)
                .userId(userId)
                .status(status)
                .deviceInfo(device)
                .build();
        }

        static Member member(UUID id, String name) {
            return Member.builder()
                .id(id)
                .name(name)
                .avatar("avatar")
                .backgroundColor("#FFFFFF")
                .build();
        }

        static WebPushRegisterDto reqRegister(String token) {
            return new WebPushRegisterDto(
                token,
                "https://fcm.googleapis.com/fcm/send/test123",
                "p256dh-key",
                "auth-key"
            );
        }
    }
}
