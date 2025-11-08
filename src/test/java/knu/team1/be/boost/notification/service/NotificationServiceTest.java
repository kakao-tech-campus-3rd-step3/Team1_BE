package knu.team1.be.boost.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.notification.dto.NotificationCountResponseDto;
import knu.team1.be.boost.notification.dto.NotificationListResponseDto;
import knu.team1.be.boost.notification.dto.NotificationReadResponseDto;
import knu.team1.be.boost.notification.dto.ProjectNotificationResponseDto;
import knu.team1.be.boost.notification.entity.Notification;
import knu.team1.be.boost.notification.event.dto.NotificationType;
import knu.team1.be.boost.notification.repository.NotificationRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    NotificationRepository notificationRepository;
    @Mock
    ProjectMembershipRepository projectMembershipRepository;
    @Mock
    AccessPolicy accessPolicy;
    @Mock
    NotificationSenderService notificationSenderService;

    NotificationService notificationService;

    UUID userId;
    UserPrincipalDto user;
    Member member;
    UUID projectId;
    Project project;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = UserPrincipalDto.from(userId, "테스터", "avatar");
        member = Fixtures.member(userId, "테스터");
        projectId = UUID.randomUUID();
        project = Fixtures.project(projectId);

        notificationService = new NotificationService(
            taskRepository,
            memberRepository,
            projectRepository,
            notificationRepository,
            projectMembershipRepository,
            accessPolicy,
            notificationSenderService
        );
    }

    @Nested
    @DisplayName("알림 목록 조회")
    class GetNotifications {

        @Test
        @DisplayName("알림 목록 조회 성공 - 커서 없이 정상 조회")
        void success_noCursor() {
            given(memberRepository.findById(userId)).willReturn(Optional.of(member));
            Notification n1 = Fixtures.notification(UUID.randomUUID(), member, "title1", "msg1");
            given(notificationRepository.findByMemberWithCursor(eq(member), isNull(), isNull(),
                any(Pageable.class)))
                .willReturn(List.of(n1));

            NotificationListResponseDto res = notificationService.getNotifications(null, 10,
                userId);

            assertThat(res.notifications()).hasSize(1);
            assertThat(res.count()).isEqualTo(1);
            verify(notificationRepository).findByMemberWithCursor(eq(member), isNull(), isNull(),
                any());
        }

        @Test
        @DisplayName("알림 목록 조회 실패 - MEMBER_NOT_FOUND")
        void fail_memberNotFound() {
            given(memberRepository.findById(userId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> notificationService.getNotifications(null, 10, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("알림 목록 조회 실패 - 커서가 다른 사용자 알림")
        void fail_invalidCursor() {
            Notification cursor = Fixtures.notification(UUID.randomUUID(),
                Fixtures.member(UUID.randomUUID(), "다른유저"), "t", "m");
            given(memberRepository.findById(userId)).willReturn(Optional.of(member));
            given(notificationRepository.findById(cursor.getId())).willReturn(Optional.of(cursor));

            assertThatThrownBy(
                () -> notificationService.getNotifications(cursor.getId(), 10, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CURSOR);
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("알림 읽음 처리 성공 - 본인 알림 읽음 처리")
        void success() {
            Notification n = Fixtures.notification(UUID.randomUUID(), member, "title", "msg");
            given(notificationRepository.findById(n.getId())).willReturn(Optional.of(n));

            NotificationReadResponseDto res = notificationService.markAsRead(n.getId(), userId);

            assertThat(res.read()).isTrue();
            verify(notificationRepository).findById(n.getId());
        }

        @Test
        @DisplayName("알림 읽음 처리 실패 - NOTIFICATION_NOT_FOUND")
        void fail_notFound() {
            UUID nId = UUID.randomUUID();
            given(notificationRepository.findById(nId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead(nId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        @Test
        @DisplayName("알림 읽음 처리 실패 - 다른 사람 알림 접근")
        void fail_forbiddenAccess() {
            Notification n = Fixtures.notification(UUID.randomUUID(),
                Fixtures.member(UUID.randomUUID(), "다른유저"), "t", "m");
            given(notificationRepository.findById(n.getId())).willReturn(Optional.of(n));

            assertThatThrownBy(() -> notificationService.markAsRead(n.getId(), userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_FORBIDDEN_ACCESS);
        }
    }

    @Nested
    @DisplayName("모든 알림 읽음 처리 (Service)")
    class MarkAllAsRead {

        @Test
        @DisplayName("모든 알림 읽음 처리 성공 - 미읽은 알림이 있을 때 bulk update 수행")
        void success_bulkUpdateCalled() {
            // given
            given(memberRepository.findById(userId)).willReturn(Optional.of(member));

            // when
            notificationService.markAllAsRead(userId);

            // then
            verify(memberRepository).findById(userId);
            verify(notificationRepository).markAllAsReadByMember(member);
        }

        @Test
        @DisplayName("실패 - MEMBER_NOT_FOUND")
        void fail_memberNotFound() {
            // given
            given(memberRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.markAllAsRead(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("프로젝트 알림 설정 변경")
    class SetProjectNotification {

        @Test
        @DisplayName("프로젝트 알림 설정 변경 성공 - 알림 ON")
        void success_enable() {
            ProjectMembership pm = Fixtures.projectMembership(project, member);
            pm.updateNotificationEnabled(false);

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            doNothing().when(accessPolicy).ensureProjectMember(projectId, userId);
            given(projectMembershipRepository.findByProjectIdAndMemberId(projectId, userId))
                .willReturn(Optional.of(pm));

            ProjectNotificationResponseDto res = notificationService.setProjectNotification(
                projectId, true, userId);

            assertThat(res.enabled()).isTrue();
            verify(accessPolicy).ensureProjectMember(projectId, userId);
        }

        @Test
        @DisplayName("프로젝트 알림 설정 변경 실패 - PROJECT_NOT_FOUND")
        void fail_projectNotFound() {
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());
            assertThatThrownBy(
                () -> notificationService.setProjectNotification(projectId, true, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);
        }

        @Test
        @DisplayName("프로젝트 알림 설정 변경 실패 - PROJECT_MEMBER_NOT_FOUND")
        void fail_memberNotFound() {
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            doNothing().when(accessPolicy).ensureProjectMember(projectId, userId);
            given(projectMembershipRepository.findByProjectIdAndMemberId(projectId, userId))
                .willReturn(Optional.empty());

            assertThatThrownBy(
                () -> notificationService.setProjectNotification(projectId, false, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("작업 리뷰 알림 전송")
    class NotifyTaskReview {

        @Test
        @DisplayName("작업 리뷰 알림 전송 성공 - 알림 전송 시도")
        void success() {
            Task task = Fixtures.task(UUID.randomUUID(), project);
            ProjectMembership pm1 = Fixtures.projectMembership(project, member);
            project.getProjectMemberships().add(pm1);

            given(projectRepository.findByIdWithMemberships(projectId)).willReturn(
                Optional.of(project));
            given(taskRepository.findByIdWithAssignees(task.getId())).willReturn(Optional.of(task));

            notificationService.notifyTaskReview(projectId, task.getId(),
                NotificationType.REVIEW);

            verify(notificationSenderService).saveAndSendNotification(
                any(Member.class), anyString(), anyString()
            );
        }

        @Test
        @DisplayName("작업 리뷰 알림 전송 실패 - PROJECT_NOT_FOUND")
        void fail_projectNotFound() {
            given(projectRepository.findByIdWithMemberships(projectId)).willReturn(
                Optional.empty());
            assertThatThrownBy(
                () -> notificationService.notifyTaskReview(projectId, UUID.randomUUID(),
                    NotificationType.REVIEW))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);
        }

        @Test
        @DisplayName("작업 리뷰 알림 전송 실패 - TASK_NOT_FOUND")
        void fail_taskNotFound() {
            given(projectRepository.findByIdWithMemberships(projectId))
                .willReturn(Optional.of(project));
            given(taskRepository.findByIdWithAssignees(any()))
                .willReturn(Optional.empty());

            assertThatThrownBy(
                () -> notificationService.notifyTaskReview(projectId, UUID.randomUUID(),
                    NotificationType.REVIEW))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("작업 승인 알림 전송")
    class NotifyTaskApprove {

        @Test
        @DisplayName("작업 승인 알림 전송 성공 - 담당자에게 알림 발송")
        void success() {
            Task task = Fixtures.task(UUID.randomUUID(), project);
            Member assignee = Fixtures.member(UUID.randomUUID(), "담당자");
            task.getAssignees().add(assignee);

            ProjectMembership pm = Fixtures.projectMembership(project, assignee);
            project.getProjectMemberships().add(pm);

            given(projectRepository.findByIdWithMemberships(projectId)).willReturn(
                Optional.of(project));
            given(taskRepository.findByIdWithAssignees(task.getId())).willReturn(Optional.of(task));

            notificationService.notifyTaskApprove(projectId, task.getId());

            verify(notificationSenderService).saveAndSendNotification(
                any(Member.class), eq(NotificationType.APPROVED.title()), anyString()
            );
        }

        @Test
        @DisplayName("작업 승인 알림 전송 실패 - PROJECT_NOT_FOUND")
        void fail_projectNotFound() {
            given(projectRepository.findByIdWithMemberships(projectId)).willReturn(
                Optional.empty());
            assertThatThrownBy(
                () -> notificationService.notifyTaskApprove(projectId, UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("알림 개수 조회")
    class GetNotificationCount {

        @Test
        @DisplayName("알림 개수 조회 성공 - 정상 개수 반환")
        void success() {
            given(memberRepository.findById(userId)).willReturn(Optional.of(member));
            given(notificationRepository.countByMember(member)).willReturn(5L);
            given(notificationRepository.countByMemberAndIsReadFalse(member)).willReturn(2L);

            NotificationCountResponseDto res = notificationService.getNotificationCount(userId);

            assertThat(res.totalCount()).isEqualTo(5);
            assertThat(res.unreadCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("알림 개수 조회 실패 - MEMBER_NOT_FOUND")
        void fail_memberNotFound() {
            given(memberRepository.findById(userId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> notificationService.getNotificationCount(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
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

        static Notification notification(UUID id, Member member, String title, String message) {
            return Notification.builder()
                .id(id)
                .member(member)
                .title(title)
                .message(message)
                .isRead(false)
                .build();
        }

        static Project project(UUID id) {
            return Project.builder()
                .id(id)
                .name("프로젝트")
                .defaultReviewerCount(1)
                .projectMemberships(new ArrayList<>())
                .build();
        }

        static ProjectMembership projectMembership(Project project, Member member) {
            return ProjectMembership.builder()
                .project(project)
                .member(member)
                .notificationEnabled(true)
                .build();
        }

        static Task task(UUID id, Project project) {
            return Task.builder()
                .id(id)
                .project(project)
                .title("테스트 작업")
                .description("내용")
                .status(knu.team1.be.boost.task.entity.TaskStatus.TODO)
                .dueDate(java.time.LocalDate.now())
                .urgent(false)
                .requiredReviewerCount(1)
                .tags(new HashSet<>())
                .assignees(new HashSet<>())
                .build();
        }
    }
}
