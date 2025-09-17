package knu.team1.be.boost.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.exception.MemberNotFoundException;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.exception.ProjectNotFoundException;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.dto.TaskStatusRequestDto;
import knu.team1.be.boost.task.dto.TaskUpdateRequestDto;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.exception.TaskNotFoundException;
import knu.team1.be.boost.task.exception.TaskNotInProjectException;
import knu.team1.be.boost.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    ProjectRepository projectRepository;

    TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, memberRepository, projectRepository);
    }

    private Project project(UUID id) {
        return Project.builder().id(id).name("테스트 프로젝트").build();
    }

    private Member member(UUID id, String name) {
        return Member.builder().id(id).name(name).build();
    }

    private Task task(UUID taskId, Project project) {
        return Task.builder()
            .id(taskId)
            .project(project)
            .title("1회차 기술 멘토링 피드백 반영")
            .description("기술 멘토링에서 나온 멘토님의 피드백을 반영한다.")
            .status(TaskStatus.TODO)
            .dueDate(LocalDate.of(2025, 9, 17))
            .urgent(false)
            .requiredReviewerCount(1)
            .tags(new ArrayList<>(List.of("피드백", "멘토링")))
            .assignees(Set.of())
            .build();
    }

    @Nested
    @DisplayName("할 일 생성")
    class CreateTask {

        @Test
        @DisplayName("할 일 생성 성공")
        void test1() {
            // given
            UUID projectId = UUID.randomUUID();
            Project project = project(projectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            UUID m1 = UUID.randomUUID();
            UUID m2 = UUID.randomUUID();
            given(memberRepository.findAllById(anyList()))
                .willReturn(List.of(member(m1, "영진"), member(m2, "비버")));

            TaskCreateRequestDto request = new TaskCreateRequestDto(
                "1회차 기술 멘토링 피드백 반영",
                "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                LocalDate.of(2025, 9, 17),
                false,
                1,
                List.of("피드백", "멘토링"),
                List.of(m1, m2)
            );

            given(taskRepository.save(any(Task.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            TaskResponseDto response = taskService.createTask(projectId, request);

            // then
            assertThat(response.projectId()).isEqualTo(projectId);
            assertThat(response.title()).isEqualTo("1회차 기술 멘토링 피드백 반영");
            assertThat(response.description()).isEqualTo("기술 멘토링에서 나온 멘토님의 피드백을 반영한다.");
            assertThat(response.status()).isEqualTo(TaskStatus.TODO);
            assertThat(response.dueDate()).isEqualTo(LocalDate.of(2025, 9, 17));
            assertThat(response.urgent()).isFalse();
            assertThat(response.requiredReviewerCount()).isEqualTo(1);
            assertThat(response.tags()).containsExactlyInAnyOrder("피드백", "멘토링");
            assertThat(response.assignees()).hasSize(2);

            ArgumentCaptor<Task> savedCap = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(savedCap.capture());
            Task saved = savedCap.getValue();
            assertThat(saved.getProject()).isEqualTo(project);
            assertThat(saved.getAssignees()).extracting("id")
                .containsExactlyInAnyOrder(m1, m2);
        }

        @Test
        @DisplayName("할 일 생성 실패 - 404 (프로젝트 없음)")
        void test2() {
            // given
            UUID projectId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            TaskCreateRequestDto request = new TaskCreateRequestDto(
                "1회차 기술 멘토링 피드백 반영",
                "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                LocalDate.of(2025, 9, 17),
                false,
                1,
                List.of("피드백", "멘토링"),
                List.of()
            );

            // when & then
            assertThatThrownBy(() -> taskService.createTask(projectId, request))
                .isInstanceOf(ProjectNotFoundException.class);

            verifyNoInteractions(taskRepository, memberRepository);
        }

        @Test
        @DisplayName("할 일 생성 실패 - 404 (담당자 일부 없음)")
        void test3() {
            // given
            UUID projectId = UUID.randomUUID();
            Project project = project(projectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            UUID m1 = UUID.randomUUID();
            UUID missing = UUID.randomUUID();
            given(memberRepository.findAllById(anyList()))
                .willReturn(List.of(member(m1, "비버")));

            TaskCreateRequestDto request = new TaskCreateRequestDto(
                "1회차 기술 멘토링 피드백 반영",
                "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                LocalDate.of(2025, 9, 17),
                false,
                1,
                List.of("피드백", "멘토링"),
                List.of(m1, missing)
            );

            // when & then
            assertThatThrownBy(() -> taskService.createTask(projectId, request))
                .isInstanceOf(MemberNotFoundException.class);

            verify(taskRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("할 일 수정")
    class UpdateTask {

        @Test
        @DisplayName("할 일 수정 성공")
        void test1() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();

            Project project = project(projectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = task(taskId, project);
            given(taskRepository.findById(taskId)).willReturn(Optional.of(existing));

            UUID m1 = UUID.randomUUID();
            given(memberRepository.findAllById(anyList()))
                .willReturn(List.of(member(m1, "비버")));

            TaskUpdateRequestDto request = new TaskUpdateRequestDto(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.PROGRESS,
                LocalDate.of(2025, 10, 1),
                true,
                3,
                List.of("수정"),
                List.of(m1)
            );

            // when
            TaskResponseDto response = taskService.updateTask(projectId, taskId, request);

            // then
            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.description()).isEqualTo("수정된 설명");
            assertThat(response.status()).isEqualTo(TaskStatus.PROGRESS);
            assertThat(response.dueDate()).isEqualTo(LocalDate.of(2025, 10, 1));
            assertThat(response.urgent()).isTrue();
            assertThat(response.requiredReviewerCount()).isEqualTo(3);
            assertThat(response.tags()).containsExactlyInAnyOrder("수정");
            assertThat(response.assignees()).hasSize(1);
        }

        @Test
        @DisplayName("할 일 수정 실패 - 404 (프로젝트 없음)")
        void test2() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            TaskUpdateRequestDto request = new TaskUpdateRequestDto(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.PROGRESS,
                LocalDate.of(2025, 10, 1),
                true,
                3,
                List.of("수정"),
                List.of()
            );

            // when & then
            assertThatThrownBy(() -> taskService.updateTask(projectId, taskId, request))
                .isInstanceOf(ProjectNotFoundException.class);

            verifyNoInteractions(taskRepository, memberRepository);
        }

        @Test
        @DisplayName("할 일 수정 실패 - 404 (할 일 없음)")
        void test3() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(
                Optional.of(project(projectId)));
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            TaskUpdateRequestDto request = new TaskUpdateRequestDto(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.PROGRESS,
                LocalDate.of(2025, 10, 1),
                true,
                3,
                List.of("수정"),
                List.of()
            );

            // when & then
            assertThatThrownBy(() -> taskService.updateTask(projectId, taskId, request))
                .isInstanceOf(TaskNotFoundException.class);

            verifyNoInteractions(memberRepository);
        }

        @Test
        @DisplayName("할 일 수정 실패 - 409 (다른 프로젝트의 할 일)")
        void test4() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID otherProjectId = UUID.randomUUID();

            Project project = project(projectId);
            Project otherProject = project(otherProjectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = task(UUID.randomUUID(), otherProject);
            given(taskRepository.findById(existing.getId())).willReturn(Optional.of(existing));

            TaskUpdateRequestDto request = new TaskUpdateRequestDto(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.PROGRESS,
                LocalDate.of(2025, 10, 1),
                true,
                3,
                List.of("수정"),
                List.of()
            );

            // when & then
            assertThatThrownBy(() -> taskService.updateTask(projectId, existing.getId(), request))
                .isInstanceOf(TaskNotInProjectException.class);

            verifyNoInteractions(memberRepository);
        }

    }

    @Nested
    @DisplayName("할 일 삭제")
    class DeleteTask {

        @Test
        @DisplayName("할 일 삭제 성공")
        void test1() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();

            Project project = project(projectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = task(taskId, project);
            given(taskRepository.findById(taskId)).willReturn(Optional.of(existing));

            // when
            taskService.deleteTask(projectId, taskId);

            // then
            verify(taskRepository).delete(existing);
        }

        @Test
        @DisplayName("할 일 삭제 실패 - 404 (프로젝트 없음)")
        void test2() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.deleteTask(projectId, taskId))
                .isInstanceOf(ProjectNotFoundException.class);

            verifyNoInteractions(taskRepository);
        }


        @Test
        @DisplayName("할 일 삭제 실패 - 404 (할 일 없음)")
        void test3() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();

            Project project = project(projectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.deleteTask(projectId, taskId))
                .isInstanceOf(TaskNotFoundException.class);

            verify(taskRepository, never()).delete(any());
        }

        @Test
        @DisplayName("할 일 삭제 실패 - 409 (다른 프로젝트의 할 일)")
        void test4() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID otherProjectId = UUID.randomUUID();

            Project project = project(projectId);
            Project otherProject = project(otherProjectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = task(UUID.randomUUID(), otherProject);
            given(taskRepository.findById(existing.getId())).willReturn(Optional.of(existing));

            // when & then
            assertThatThrownBy(() -> taskService.deleteTask(projectId, existing.getId()))
                .isInstanceOf(TaskNotInProjectException.class);

            verify(taskRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("할 일 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("할 일 상태 변경 성공")
        void test1() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();

            Project project = project(projectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = task(taskId, project);
            given(taskRepository.findById(taskId)).willReturn(Optional.of(existing));

            TaskStatusRequestDto request = new TaskStatusRequestDto(TaskStatus.REVIEW);

            // when
            TaskResponseDto response = taskService.changeTaskStatus(projectId, taskId, request);

            // then
            assertThat(response.status()).isEqualTo(TaskStatus.REVIEW);
            assertThat(existing.getStatus()).isEqualTo(TaskStatus.REVIEW);
        }


        @Test
        @DisplayName("할 일 상태 변경 실패 - 404 (프로젝트 없음)")
        void test2() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            TaskStatusRequestDto request = new TaskStatusRequestDto(TaskStatus.DONE);

            // when & then
            assertThatThrownBy(() -> taskService.changeTaskStatus(projectId, taskId, request))
                .isInstanceOf(ProjectNotFoundException.class);

            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("할 일 상태 변경 실패 - 404 (할 일 없음)")
        void test3() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            given(projectRepository.findById(projectId)).willReturn(
                Optional.of(project(projectId)));
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            TaskStatusRequestDto request = new TaskStatusRequestDto(TaskStatus.DONE);

            // when & then
            assertThatThrownBy(() -> taskService.changeTaskStatus(projectId, taskId, request))
                .isInstanceOf(TaskNotFoundException.class);
        }

        @Test
        @DisplayName("할 일 상태 변경 실패 - 409 (다른 프로젝트의 할 일)")
        void test4() {
            // given
            UUID projectId = UUID.randomUUID();
            UUID anotherProjectId = UUID.randomUUID();

            Project project = project(projectId);
            Project otherProject = project(anotherProjectId);
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

            Task existing = task(UUID.randomUUID(), otherProject);
            given(taskRepository.findById(existing.getId())).willReturn(Optional.of(existing));

            TaskStatusRequestDto request = new TaskStatusRequestDto(TaskStatus.DONE);

            // when & then
            assertThatThrownBy(
                () -> taskService.changeTaskStatus(projectId, existing.getId(), request))
                .isInstanceOf(TaskNotInProjectException.class);
        }
    }
}
