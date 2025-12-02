package knu.team1.be.boost.notification.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.notification.service.NotificationSenderService;
import knu.team1.be.boost.task.repository.TaskRepository;
import knu.team1.be.boost.task.repository.TaskRepository.DueTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    TaskRepository taskRepository;
    @Mock
    NotificationSenderService notificationSenderService;

    NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationScheduler(memberRepository, taskRepository,
            notificationSenderService);
    }

    @Nested
    @DisplayName("notifyDueTomorrowTasks()")
    class NotifyDueTomorrowTasks {

        @Test
        @DisplayName("성공 - 멤버별로 알림 메시지 전송")
        void success_sendNotification() {
            // given
            LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);

            UUID memberId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();

            DueTask task1 = mock(DueTask.class);
            given(task1.getMemberId()).willReturn(memberId);
            given(task1.getProjectId()).willReturn(projectId);
            given(task1.getProjectName()).willReturn("프로젝트A");
            given(task1.getTaskTitle()).willReturn("API 구현");

            DueTask task2 = mock(DueTask.class);
            given(task2.getMemberId()).willReturn(memberId);
            given(task2.getProjectId()).willReturn(projectId);
            given(task2.getTaskTitle()).willReturn("테스트 코드 작성");

            given(taskRepository.findDueTasksByMember(tomorrow))
                .willReturn(List.of(task1, task2));

            Member member = Fixtures.member(memberId, "홍길동");
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            scheduler.notifyDueTomorrowTasks();

            // then
            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

            verify(notificationSenderService)
                .saveAndSendNotification(eq(member), titleCaptor.capture(),
                    messageCaptor.capture());

            assertThat(titleCaptor.getValue()).contains("마감 임박 작업");
            assertThat(messageCaptor.getValue())
                .contains("[프로젝트A]")
                .contains("API 구현")
                .contains("테스트 코드 작성");
        }

        @Test
        @DisplayName("실패 - 멤버를 찾을 수 없음 (BusinessException 발생)")
        void fail_memberNotFound() {
            // given
            LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
            UUID memberId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();

            DueTask dueTask = mock(DueTask.class);
            given(dueTask.getMemberId()).willReturn(memberId);
            given(dueTask.getProjectId()).willReturn(projectId);

            given(taskRepository.findDueTasksByMember(tomorrow))
                .willReturn(List.of(dueTask));

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when
            scheduler.notifyDueTomorrowTasks();

            // then
            verify(notificationSenderService, never()).saveAndSendNotification(any(), any(), any());
        }

        @Test
        @DisplayName("성공 - 여러 프로젝트 작업이 각각 메시지에 포함됨")
        void success_multipleProjects() {
            LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
            UUID memberId = UUID.randomUUID();

            UUID p1 = UUID.randomUUID();
            UUID p2 = UUID.randomUUID();

            DueTask t1 = mock(DueTask.class);
            given(t1.getMemberId()).willReturn(memberId);
            given(t1.getProjectId()).willReturn(p1);
            given(t1.getProjectName()).willReturn("A프로젝트");
            given(t1.getTaskTitle()).willReturn("API 작성");

            DueTask t2 = mock(DueTask.class);
            given(t2.getMemberId()).willReturn(memberId);
            given(t2.getProjectId()).willReturn(p2);
            given(t2.getProjectName()).willReturn("B프로젝트");
            given(t2.getTaskTitle()).willReturn("UI 개발");

            given(taskRepository.findDueTasksByMember(tomorrow))
                .willReturn(List.of(t1, t2));

            Member member = Fixtures.member(memberId, "테스터");
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            scheduler.notifyDueTomorrowTasks();

            // then
            ArgumentCaptor<String> msgCap = ArgumentCaptor.forClass(String.class);
            verify(notificationSenderService)
                .saveAndSendNotification(eq(member), anyString(), msgCap.capture());

            String msg = msgCap.getValue();
            assertThat(msg).contains("[A프로젝트]").contains("[B프로젝트]");
        }
    }

    static class Fixtures {

        static Member member(UUID id, String name) {
            return Member.builder()
                .id(id)
                .name(name)
                .avatar("avatar")
                .backgroundColor("#FFFFFF")
                .notificationEnabled(true)
                .build();
        }
    }
}
