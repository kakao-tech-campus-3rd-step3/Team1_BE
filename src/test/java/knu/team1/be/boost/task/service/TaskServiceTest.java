package knu.team1.be.boost.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.comment.repository.CommentRepository;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.file.repository.FileRepository;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.tag.entity.Tag;
import knu.team1.be.boost.tag.repository.TagRepository;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.dto.TaskStatusRequestDto;
import knu.team1.be.boost.task.dto.TaskUpdateRequestDto;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TagRepository tagRepository;
    @Mock
    TaskRepository taskRepository;
    @Mock
    FileRepository fileRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    ProjectMembershipRepository projectMembershipRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    AccessPolicy accessPolicy;

    TaskService taskService;

    UUID userId;
    UserPrincipalDto user;
    UUID projectId;
    Project project;
    Task baseTask;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = UserPrincipalDto.from(userId, "test-user", "avatar-code");
        projectId = UUID.randomUUID();
        project = Fixtures.project(projectId);
        baseTask = Fixtures.task(UUID.randomUUID(), project);

        taskService = new TaskService(
            tagRepository,
            taskRepository,
            fileRepository,
            memberRepository,
            commentRepository,
            projectRepository,
            projectMembershipRepository,
            eventPublisher,
            accessPolicy
        );
    }

    @Nested
    @DisplayName("할 일 생성")
    class CreateTask {

        @Test
        @DisplayName("할 일 생성 성공")
        void test1() {
            // given
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            UUID m1 = UUID.randomUUID();
            UUID m2 = UUID.randomUUID();
            given(memberRepository.findAllById(anyList()))
                .willReturn(List.of(Fixtures.member(m1, "영진"), Fixtures.member(m2, "비버")));

            doNothing().when(accessPolicy)
                .ensureProjectMember(eq(projectId), eq(userId));
            doNothing().when(accessPolicy)
                .ensureAssigneesAreProjectMembers(eq(projectId), any(Set.class));

            UUID t1 = UUID.randomUUID();
            UUID t2 = UUID.randomUUID();
            given(tagRepository.findAllById(anyList()))
                .willReturn(
                    List.of(Fixtures.tag(t1, "태그1", project), Fixtures.tag(t2, "태그2", project)));

            TaskCreateRequestDto request = Fixtures.reqCreate(
                "1회차 기술 멘토링 피드백 반영",
                "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                Fixtures.DUE,
                false,
                1,
                List.of(t1, t2),
                List.of(m1, m2)
            );

            given(taskRepository.save(any(Task.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            TaskResponseDto res = taskService.createTask(projectId, request, user);

            // then
            assertThat(res.projectId()).isEqualTo(projectId);
            assertThat(res.title()).isEqualTo("1회차 기술 멘토링 피드백 반영");
            assertThat(res.description()).isEqualTo("기술 멘토링에서 나온 멘토님의 피드백을 반영한다.");
            assertThat(res.status()).isEqualTo(TaskStatus.TODO);
            assertThat(res.dueDate()).isEqualTo(Fixtures.DUE);
            assertThat(res.urgent()).isFalse();
            assertThat(res.requiredReviewerCount()).isEqualTo(1);
            assertThat(res.tags()).hasSize(2);
            assertThat(res.assignees()).hasSize(2);

            ArgumentCaptor<Task> cap = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(cap.capture());
            Task saved = cap.getValue();
            assertThat(saved.getProject()).isEqualTo(project);
            assertThat(saved.getAssignees()).extracting("id")
                .containsExactlyInAnyOrder(m1, m2);
        }

        @Test
        @DisplayName("할 일 생성 실패 - 404 (프로젝트 없음)")
        void test2() {
            // given
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            TaskCreateRequestDto request = Fixtures.reqCreate(
                "1회차 기술 멘토링 피드백 반영",
                "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                Fixtures.DUE,
                false,
                1,
                List.of(),
                List.of()
            );

            // when & then
            assertThatThrownBy(() -> taskService.createTask(projectId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

            verifyNoInteractions(taskRepository, memberRepository, accessPolicy);
        }

        @Test
        @DisplayName("할 일 생성 실패 - 404 (담당자 일부 없음)")
        void test3() {
            // given
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            UUID m1 = UUID.randomUUID();
            UUID missing = UUID.randomUUID();
            given(memberRepository.findAllById(anyList()))
                .willReturn(List.of(Fixtures.member(m1, "비버")));

            TaskCreateRequestDto request = Fixtures.reqCreate(
                "1회차 기술 멘토링 피드백 반영",
                "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                Fixtures.DUE,
                false,
                1,
                List.of(),
                List.of(m1, missing)
            );

            // when & then
            assertThatThrownBy(() -> taskService.createTask(projectId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(taskRepository, never()).save(any());
            verify(accessPolicy)
                .ensureProjectMember(eq(projectId), eq(userId));
            verify(accessPolicy, never())
                .ensureAssigneesAreProjectMembers(eq(projectId), any(Set.class));
        }

    }

    @Nested
    @DisplayName("할 일 수정")
    class UpdateTask {

        @Test
        @DisplayName("할 일 수정 성공")
        void test1() {
            // given
            UUID taskId = baseTask.getId();

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(taskRepository.findById(taskId)).willReturn(Optional.of(baseTask));

            UUID m1 = UUID.randomUUID();
            given(memberRepository.findAllById(anyList()))
                .willReturn(List.of(Fixtures.member(m1, "비버")));

            UUID t1 = UUID.randomUUID();
            given(tagRepository.findAllById(anyList()))
                .willReturn(List.of(Fixtures.tag(t1, "태그1", project)));

            doNothing().when(accessPolicy)
                .ensureProjectMember(eq(projectId), eq(userId));
            doNothing().when(accessPolicy)
                .ensureTaskAssignee(eq(taskId), eq(userId));
            doNothing().when(accessPolicy)
                .ensureAssigneesAreProjectMembers(eq(projectId), any(Set.class));

            TaskUpdateRequestDto request = Fixtures.reqUpdate(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.PROGRESS,
                Fixtures.DUE2,
                true,
                3,
                List.of(t1),
                List.of(m1)
            );

            // when
            TaskResponseDto res = taskService.updateTask(projectId, taskId, request, user);

            // then
            assertThat(res.title()).isEqualTo("수정된 제목");
            assertThat(res.description()).isEqualTo("수정된 설명");
            assertThat(res.status()).isEqualTo(TaskStatus.PROGRESS);
            assertThat(res.dueDate()).isEqualTo(Fixtures.DUE2);
            assertThat(res.urgent()).isTrue();
            assertThat(res.requiredReviewerCount()).isEqualTo(3);
            assertThat(res.tags()).hasSize(1);
            assertThat(res.assignees()).hasSize(1);
        }

        @Test
        @DisplayName("할 일 수정 실패 - 404 (프로젝트 없음)")
        void test2() {
            // given
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            TaskUpdateRequestDto request = Fixtures.reqUpdate(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.PROGRESS,
                Fixtures.DUE2,
                true,
                3,
                List.of(),
                List.of()
            );

            // when & then
            assertThatThrownBy(() -> taskService.updateTask(projectId, taskId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

            verifyNoInteractions(taskRepository, memberRepository, accessPolicy);
        }

        @Test
        @DisplayName("할 일 수정 실패 - 404 (할 일 없음)")
        void test3() {
            // given
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            TaskUpdateRequestDto request = Fixtures.reqUpdate(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.PROGRESS,
                Fixtures.DUE2,
                true,
                3,
                List.of(),
                List.of()
            );

            // when & then
            assertThatThrownBy(() -> taskService.updateTask(projectId, taskId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);

            verifyNoInteractions(memberRepository, accessPolicy);
        }

        @Test
        @DisplayName("할 일 수정 실패 - 409 (다른 프로젝트의 할 일)")
        void test4() {
            // given
            UUID otherProjectId = UUID.randomUUID();
            Project otherProject = Fixtures.project(otherProjectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = Fixtures.task(UUID.randomUUID(), otherProject);
            given(taskRepository.findById(existing.getId())).willReturn(Optional.of(existing));

            TaskUpdateRequestDto request = Fixtures.reqUpdate(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.PROGRESS,
                Fixtures.DUE2,
                true,
                3,
                List.of(),
                List.of()
            );

            // when & then
            assertThatThrownBy(
                () -> taskService.updateTask(projectId, existing.getId(), request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_IN_PROJECT);

            verifyNoInteractions(memberRepository, accessPolicy);
        }

    }

    @Nested
    @DisplayName("할 일 삭제")
    class DeleteTask {

        @Test
        @DisplayName("할 일 삭제 성공")
        void test1() {
            // given
            UUID taskId = baseTask.getId();

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(taskRepository.findById(taskId)).willReturn(Optional.of(baseTask));

            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));
            doNothing().when(accessPolicy).ensureTaskAssignee(eq(taskId), eq(userId));

            // when
            taskService.deleteTask(projectId, taskId, user);

            // then
            verify(taskRepository).delete(baseTask);
        }

        @Test
        @DisplayName("할 일 삭제 실패 - 404 (프로젝트 없음)")
        void test2() {
            // given
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.deleteTask(projectId, taskId, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

            verifyNoInteractions(taskRepository, accessPolicy);
        }

        @Test
        @DisplayName("할 일 삭제 실패 - 404 (할 일 없음)")
        void test3() {
            // given
            UUID taskId = UUID.randomUUID();

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.deleteTask(projectId, taskId, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);

            verify(taskRepository, never()).delete(any());
            verifyNoInteractions(accessPolicy);
        }

        @Test
        @DisplayName("할 일 삭제 실패 - 409 (다른 프로젝트의 할 일)")
        void test4() {
            // given
            UUID otherProjectId = UUID.randomUUID();
            Project otherProject = Fixtures.project(otherProjectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = Fixtures.task(UUID.randomUUID(), otherProject);
            given(taskRepository.findById(existing.getId())).willReturn(Optional.of(existing));

            // when & then
            assertThatThrownBy(() -> taskService.deleteTask(projectId, existing.getId(), user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_IN_PROJECT);

            verify(taskRepository, never()).delete(any());
            verifyNoInteractions(accessPolicy);
        }
    }

    @Nested
    @DisplayName("할 일 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("할 일 상태 변경 성공")
        void test1() {
            // given
            UUID taskId = baseTask.getId();

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(taskRepository.findById(taskId)).willReturn(Optional.of(baseTask));

            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));
            doNothing().when(accessPolicy).ensureTaskAssignee(eq(taskId), eq(userId));

            TaskStatusRequestDto request = Fixtures.reqStatus(TaskStatus.REVIEW);

            // when
            TaskResponseDto response = taskService.changeTaskStatus(
                projectId, taskId, request, user
            );

            // then
            assertThat(response.status()).isEqualTo(TaskStatus.REVIEW);
            assertThat(baseTask.getStatus()).isEqualTo(TaskStatus.REVIEW);
        }

        @Test
        @DisplayName("할 일 상태 변경 실패 - 404 (프로젝트 없음)")
        void test2() {
            // given
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            TaskStatusRequestDto request = Fixtures.reqStatus(TaskStatus.DONE);

            // when & then
            assertThatThrownBy(() -> taskService.changeTaskStatus(projectId, taskId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

            verifyNoInteractions(taskRepository, accessPolicy);
        }

        @Test
        @DisplayName("할 일 상태 변경 실패 - 404 (할 일 없음)")
        void test3() {
            // given
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            TaskStatusRequestDto request = Fixtures.reqStatus(TaskStatus.DONE);

            // when & then
            assertThatThrownBy(() -> taskService.changeTaskStatus(projectId, taskId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_FOUND);

            verifyNoInteractions(accessPolicy);
        }

        @Test
        @DisplayName("할 일 상태 변경 실패 - 409 (다른 프로젝트의 할 일)")
        void test4() {
            // given
            UUID anotherProjectId = UUID.randomUUID();
            Project otherProject = Fixtures.project(anotherProjectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = Fixtures.task(UUID.randomUUID(), otherProject);
            given(taskRepository.findById(existing.getId())).willReturn(Optional.of(existing));

            TaskStatusRequestDto request = Fixtures.reqStatus(TaskStatus.DONE);

            // when & then
            assertThatThrownBy(
                () -> taskService.changeTaskStatus(projectId, existing.getId(), request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_NOT_IN_PROJECT);

            verifyNoInteractions(accessPolicy);
        }
    }

    static class Fixtures {

        static final LocalDate DUE = LocalDate.of(2025, 9, 17);
        static final LocalDate DUE2 = LocalDate.of(2025, 10, 1);

        static Project project(UUID id) {
            return Project.builder().id(id).name("테스트 프로젝트").build();
        }

        static Member member(UUID id, String name) {
            return Member.builder().id(id).name(name).build();
        }

        static Tag tag(UUID id, String name, Project project) {
            return Tag.builder().id(id).name(name).project(project).build();
        }

        static Task task(UUID taskId, Project project) {
            Tag tag1 = Tag.builder().id(UUID.randomUUID()).name("피드백").build();
            Tag tag2 = Tag.builder().id(UUID.randomUUID()).name("멘토링").build();

            return Task.builder()
                .id(taskId)
                .project(project)
                .title("1회차 기술 멘토링 피드백 반영")
                .description("기술 멘토링에서 나온 멘토님의 피드백을 반영한다.")
                .status(TaskStatus.TODO)
                .dueDate(DUE)
                .urgent(false)
                .requiredReviewerCount(1)
                .tags(new ArrayList<>(List.of(tag1, tag2)))
                .assignees(Set.of())
                .build();
        }

        static TaskCreateRequestDto reqCreate(
            String title,
            String desc,
            TaskStatus status,
            LocalDate due,
            boolean urgent,
            int reviewers,
            List<UUID> tags,
            List<UUID> assignees
        ) {
            return new TaskCreateRequestDto(
                title, desc, status, due, urgent, reviewers, tags, assignees
            );
        }

        static TaskUpdateRequestDto reqUpdate(
            String title,
            String desc,
            TaskStatus status,
            LocalDate due,
            boolean urgent,
            int reviewers,
            List<UUID> tags,
            List<UUID> assignees
        ) {
            return new TaskUpdateRequestDto(
                title, desc, status, due, urgent, reviewers, tags, assignees
            );
        }

        static TaskStatusRequestDto reqStatus(TaskStatus status) {
            return new TaskStatusRequestDto(status);
        }
    }
}
