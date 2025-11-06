package knu.team1.be.boost.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.notification.entity.Notification;
import knu.team1.be.boost.notification.event.NotificationEventPublisher;
import knu.team1.be.boost.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSenderServiceTest {

    @Mock
    NotificationRepository notificationRepository;
    @Mock
    NotificationEventPublisher notificationEventPublisher;

    NotificationSenderService senderService;

    @BeforeEach
    void setUp() {
        senderService = new NotificationSenderService(notificationRepository,
            notificationEventPublisher);
    }

    @Nested
    @DisplayName("알림 저장 및 발송")
    class SaveAndSend {

        @Test
        @DisplayName("성공 - 알림 저장 및 이벤트 발행 (알림 활성화 ON)")
        void success_sendEnabled() {
            // given
            Member member = Member.builder()
                .id(java.util.UUID.randomUUID())
                .name("테스터")
                .avatar("avatar")
                .backgroundColor("#FFFFFF")
                .notificationEnabled(true)
                .build();

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            senderService.saveAndSendNotification(member, "테스트 제목", "테스트 메시지");

            // then
            verify(notificationRepository).save(captor.capture());
            verify(notificationEventPublisher).publishNotificationSavedEvent(member, "테스트 제목",
                "테스트 메시지");

            Notification saved = captor.getValue();
            assertThat(saved.getTitle()).isEqualTo("테스트 제목");
            assertThat(saved.getMessage()).isEqualTo("테스트 메시지");
            assertThat(saved.getMember()).isEqualTo(member);
            assertThat(saved.isRead()).isFalse();
        }

        @Test
        @DisplayName("성공 - 알림 저장만 (알림 활성화 OFF)")
        void success_sendDisabled() {
            // given
            Member member = Member.builder()
                .id(java.util.UUID.randomUUID())
                .name("테스터")
                .avatar("avatar")
                .backgroundColor("#FFFFFF")
                .notificationEnabled(false)
                .build();

            when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            senderService.saveAndSendNotification(member, "비활성화 테스트", "메시지");

            // then
            verify(notificationRepository).save(any(Notification.class));
            verify(notificationEventPublisher, never()).publishNotificationSavedEvent(any(), any(),
                any());
        }
    }
}
