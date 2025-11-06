package knu.team1.be.boost.webPush.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.webPush.entity.WebPushSubscription;
import knu.team1.be.boost.webPush.repository.WebPushRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WebPushClientTest {

    @Mock
    WebPushRepository webPushRepository;
    @Mock
    PushService pushService;
    @Mock
    HttpResponse httpResponse;
    @Mock
    StatusLine statusLine;

    WebPushClient webPushClient;
    Member member;
    WebPushSubscription sub1;
    WebPushSubscription sub2;

    @BeforeEach
    void setUp()
        throws JoseException, GeneralSecurityException, IOException, ExecutionException, InterruptedException {

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        webPushClient = new WebPushClient(webPushRepository);
        ReflectionTestUtils.setField(webPushClient, "pushService", pushService);

        member = Member.builder()
            .id(UUID.randomUUID())
            .name("테스트유저")
            .avatar("avatar")
            .backgroundColor("#FFFFFF")
            .build();

        String validPublicKey =
            "BL2E3hdaS8u-UiEyzq1hILYeEjlkSMI7wS8o7mwZ3WNuo3J5qy35EuIdtj4Zrp4lHzIdvidwWFhQTEU2_vczFo8";

        sub1 = WebPushSubscription.builder()
            .id(UUID.randomUUID())
            .member(member)
            .token("t1")
            .deviceInfo("d1")
            .webPushUrl("https://fcm.googleapis.com/fcm/send/1")
            .publicKey(validPublicKey)
            .authKey("auth1")
            .build();

        sub2 = WebPushSubscription.builder()
            .id(UUID.randomUUID())
            .member(member)
            .token("t2")
            .deviceInfo("d2")
            .webPushUrl("https://fcm.googleapis.com/fcm/send/2")
            .publicKey(validPublicKey)
            .authKey("auth2")
            .build();

        lenient().doAnswer(invocation -> httpResponse).when(pushService)
            .send(any(Notification.class));
        lenient().when(httpResponse.getStatusLine()).thenReturn(statusLine);
    }

    @Test
    @DisplayName("웹푸시 알림 전송 성공 (정상 응답)")
    void success_sendNotification() throws Exception {
        given(webPushRepository.findByMemberId(member.getId())).willReturn(List.of(sub1));
        given(statusLine.getStatusCode()).willReturn(201);

        webPushClient.sendNotification(member, "테스트 제목", "본문");

        verify(pushService, times(1)).send(any(Notification.class));
        verify(webPushRepository, never()).delete(any());
    }

    @Test
    @DisplayName("404/410 응답 시 구독 삭제")
    void delete_on404or410() {
        given(webPushRepository.findByMemberId(member.getId())).willReturn(List.of(sub1));
        given(statusLine.getStatusCode()).willReturn(404);

        webPushClient.sendNotification(member, "삭제 테스트", "본문");

        verify(webPushRepository).delete(eq(sub1));
    }

    @Test
    @DisplayName("500 응답 시 구독 유지 (삭제되지 않음)")
    void error_500_keepsSubscription() {
        given(webPushRepository.findByMemberId(member.getId())).willReturn(List.of(sub1));
        given(statusLine.getStatusCode()).willReturn(500);
        given(statusLine.getReasonPhrase()).willReturn("Internal Server Error");

        webPushClient.sendNotification(member, "서버오류", "본문");

        verify(webPushRepository, never()).delete(any());
    }

    @Test
    @DisplayName("여러 디바이스 동시 전송")
    void multiDevice_sendToAll() throws Exception {
        given(webPushRepository.findByMemberId(member.getId())).willReturn(List.of(sub1, sub2));
        given(statusLine.getStatusCode()).willReturn(201);

        webPushClient.sendNotification(member, "다중 디바이스", "본문");

        verify(pushService, times(2)).send(any(Notification.class));
    }

    @Test
    @DisplayName("구독이 없는 경우 처리 (예외 없이 종료)")
    void noSubscriptions_doesNothing() throws Exception {
        given(webPushRepository.findByMemberId(member.getId())).willReturn(List.of());

        webPushClient.sendNotification(member, "구독없음", "본문");

        verify(pushService, never()).send(any());
        verify(webPushRepository, never()).delete(any());
    }

    @Test
    @DisplayName("예외 발생 시 로그만 남기고 중단되지 않음")
    void handleException() throws Exception {
        when(pushService.send(any(Notification.class)))
            .thenThrow(new RuntimeException("전송 실패"));
        given(webPushRepository.findByMemberId(member.getId())).willReturn(List.of(sub1));

        webPushClient.sendNotification(member, "예외테스트", "본문");

        verify(webPushRepository, never()).delete(any());
    }
}
