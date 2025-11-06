package knu.team1.be.boost.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.security.filter.JwtAuthFilter;
import knu.team1.be.boost.tag.dto.TagResponseDto;
import knu.team1.be.boost.task.dto.MemberTaskStatusCountResponseDto;
import knu.team1.be.boost.task.dto.MyTaskStatusCountResponseDto;
import knu.team1.be.boost.task.dto.ProjectTaskStatusCountResponseDto;
import knu.team1.be.boost.task.dto.TaskApproveResponseDto;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskDetailResponseDto;
import knu.team1.be.boost.task.dto.TaskMemberSectionResponseDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.dto.TaskStatusRequestDto;
import knu.team1.be.boost.task.dto.TaskStatusSectionDto;
import knu.team1.be.boost.task.dto.TaskUpdateRequestDto;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = TaskController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Nested
    @DisplayName("할 일 생성")
    class CreateTask {

        @Test
        @DisplayName("성공 - 201 Created + Location 헤더")
        void createTask_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();
            UUID tag1 = Fixtures.id();
            UUID tag2 = Fixtures.id();
            UUID member1 = Fixtures.id();
            UUID member2 = Fixtures.id();

            TaskCreateRequestDto request = Fixtures.reqCreate(
                "1회차 기술 멘토링 피드백 반영",
                "멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                Fixtures.DUE,
                true,
                2,
                List.of(tag1, tag2),
                List.of(member1, member2)
            );

            TaskResponseDto response = Fixtures.taskResponse(
                taskId, projectId, "1회차 기술 멘토링 피드백 반영",
                "멘토님의 피드백을 반영한다.", TaskStatus.TODO, true, 2,
                List.of(new TagResponseDto(tag1, "피드백"), new TagResponseDto(tag2, "멘토링")),
                List.of(Fixtures.memberDto(member1, "김부스트"), Fixtures.memberDto(member2, "이부스트"))
            );

            given(taskService.createTask(eq(projectId), any(TaskCreateRequestDto.class), any()))
                .willReturn(response);

            mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                    "/api/projects/" + projectId + "/tasks/" + taskId))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.tags[0].name").value("피드백"))
                .andExpect(jsonPath("$.tags[1].name").value("멘토링"));
        }

        @Test
        @DisplayName("실패 - 400 (빈 제목)")
        void createTask_fail_emptyTitle() throws Exception {
            TaskCreateRequestDto request = Fixtures.reqCreate(
                "", "설명", TaskStatus.TODO,
                Fixtures.DUE, false, 1, List.of(Fixtures.id()), List.of()
            );
            mockMvc.perform(post("/api/projects/{projectId}/tasks", Fixtures.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 400 (마감일 null)")
        void createTask_fail_nullDeadline() throws Exception {
            TaskCreateRequestDto request = Fixtures.reqCreate(
                "제목", "설명", TaskStatus.TODO,
                null, false, 1, List.of(Fixtures.id()), List.of()
            );
            mockMvc.perform(post("/api/projects/{projectId}/tasks", Fixtures.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 400 (requiredReviewerCount < 0)")
        void createTask_fail_negativeReviewerCount() throws Exception {
            TaskCreateRequestDto request = Fixtures.reqCreate(
                "제목", "설명", TaskStatus.TODO,
                Fixtures.DUE, false, -1, List.of(Fixtures.id()), List.of()
            );
            mockMvc.perform(post("/api/projects/{projectId}/tasks", Fixtures.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("할 일 수정")
    class UpdateTask {

        @Test
        @DisplayName("성공 - 200 OK")
        void updateTask_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();
            UUID tagId = Fixtures.id();

            TaskUpdateRequestDto request = Fixtures.reqUpdate(
                "수정된 제목", "수정된 설명", TaskStatus.REVIEW,
                Fixtures.DUE, false, 1, List.of(tagId), List.of()
            );

            TaskResponseDto response = Fixtures.taskResponse(
                taskId, projectId, "수정된 제목", "수정된 설명",
                TaskStatus.REVIEW, false, 1,
                List.of(new TagResponseDto(tagId, "멘토링")), List.of()
            );

            given(taskService.updateTask(eq(projectId), eq(taskId), any(), any()))
                .willReturn(response);

            mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.tags[0].name").value("멘토링"));
        }

        @Test
        @DisplayName("실패 - 400 (빈 제목)")
        void updateTask_fail_emptyTitle() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();

            TaskUpdateRequestDto request = Fixtures.reqUpdate(
                "", "설명", TaskStatus.PROGRESS,
                Fixtures.DUE, false, 0, List.of(), List.of()
            );

            mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 400 (마감일 null)")
        void updateTask_fail_nullDeadline() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();

            TaskUpdateRequestDto request = Fixtures.reqUpdate(
                "제목", "설명", TaskStatus.PROGRESS,
                null, false, 0, List.of(), List.of()
            );

            mockMvc.perform(put("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("할 일 삭제")
    class DeleteTask {

        @Test
        @DisplayName("성공 - 204 No Content")
        void deleteTask_success() throws Exception {
            mockMvc.perform(
                    delete("/api/projects/{projectId}/tasks/{taskId}", Fixtures.id(), Fixtures.id()))
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("할 일 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("성공 - 200 OK")
        void changeStatus_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();

            TaskStatusRequestDto request = Fixtures.reqStatus(TaskStatus.REVIEW);
            TaskResponseDto response = Fixtures.taskResponse(
                taskId, projectId, "제목", "설명",
                TaskStatus.REVIEW, true, 2,
                List.of(new TagResponseDto(Fixtures.id(), "피드백")), List.of()
            );

            given(taskService.changeTaskStatus(eq(projectId), eq(taskId), any(), any()))
                .willReturn(response);

            mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEW"));
        }

        @Test
        @DisplayName("실패 - 400 (상태 null)")
        void changeStatus_fail_nullStatus() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();

            TaskStatusRequestDto request = Fixtures.reqStatus(null);

            mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("할 일 조회 API")
    class TaskQuery {

        @Test
        @DisplayName("할 일 상세 조회 성공 - 200 OK")
        void getTaskDetail_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();

            TaskDetailResponseDto response = Fixtures.taskDetail(
                taskId,
                "1회차 기술 멘토링 피드백 반영",
                "멘토님의 피드백을 반영한다.",
                TaskStatus.REVIEW.name(),
                Fixtures.DUE,
                true,
                1,
                2,
                true,
                List.of(new TagResponseDto(Fixtures.id(), "피드백")),
                List.of(Fixtures.memberDto(Fixtures.id(), "김부스트"))
            );

            given(taskService.getTaskDetail(eq(projectId), eq(taskId), any(UserPrincipalDto.class)))
                .willReturn(response);

            mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}", projectId, taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.status").value("REVIEW"))
                .andExpect(jsonPath("$.tags[0].name").value("피드백"));
        }

        @Test
        @DisplayName("프로젝트별 할 일 목록 조회 성공 - 200 OK")
        void listTasksByStatus_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();

            TaskResponseDto task = Fixtures.taskResponse(
                taskId, projectId, "멘토링 피드백 반영", "설명",
                TaskStatus.TODO, false, 2,
                List.of(new TagResponseDto(Fixtures.id(), "피드백")),
                List.of(Fixtures.memberDto(Fixtures.id(), "김부스트"))
            );

            TaskStatusSectionDto response =
                Fixtures.statusSection(List.of(task), 1, null, false);

            given(taskService.listByStatus(
                eq(projectId),
                any(), any(), any(), any(), any(), any(), anyInt(), any()
            )).willReturn(response);

            mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                    .param("status", "TODO")
                    .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].title").value("멘토링 피드백 반영"))
                .andExpect(jsonPath("$.tasks[0].status").value("TODO"));
        }

        @Test
        @DisplayName("내 할 일 목록 조회 성공 - 200 OK")
        void listMyTasksByStatus_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();

            TaskResponseDto task = Fixtures.taskResponse(
                taskId, projectId, "내 할 일 테스트", "설명",
                TaskStatus.PROGRESS, false, 1,
                List.of(new TagResponseDto(Fixtures.id(), "개발")),
                List.of(Fixtures.memberDto(Fixtures.id(), "김부스트"))
            );

            TaskStatusSectionDto response =
                Fixtures.statusSection(List.of(task), 1, null, false);

            given(taskService.listMyTasksByStatus(
                any(), any(), any(), any(), any(), anyInt(), any()
            )).willReturn(response);

            mockMvc.perform(get("/api/me/tasks")
                    .param("status", "PROGRESS")
                    .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].title").value("내 할 일 테스트"))
                .andExpect(jsonPath("$.tasks[0].status").value("PROGRESS"));
        }

        @Test
        @DisplayName("특정 팀원의 할 일 목록 조회 성공 - 200 OK")
        void listTasksByMember_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID memberId = Fixtures.id();
            UUID taskId = Fixtures.id();

            MemberResponseDto member = Fixtures.memberDto(memberId, "이부스트");
            TaskResponseDto task = Fixtures.taskResponse(
                taskId, projectId, "팀원 할 일", "설명",
                TaskStatus.REVIEW, false, 1,
                List.of(new TagResponseDto(Fixtures.id(), "리뷰")),
                List.of(member)
            );

            TaskMemberSectionResponseDto response =
                Fixtures.memberSection(member, List.of(task), 1, null, false);

            given(taskService.listByMember(
                eq(projectId),
                eq(memberId),
                any(),
                any(),
                anyInt(),
                any(UserPrincipalDto.class)
            )).willReturn(response);

            mockMvc.perform(
                    get("/api/projects/{projectId}/members/{memberId}/tasks", projectId, memberId)
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.name").value("이부스트"))
                .andExpect(jsonPath("$.tasks[0].title").value("팀원 할 일"));
        }
    }

    @Nested
    @DisplayName("할 일 승인 및 재검토 요청 (Review 관련)")
    class Review {

        @Test
        @DisplayName("할 일 승인 성공 - 200 OK")
        void approveTask_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID taskId = Fixtures.id();

            TaskApproveResponseDto response =
                new TaskApproveResponseDto(taskId, TaskStatus.REVIEW.name(), 1, 2);

            given(taskService.approveTask(eq(projectId), eq(taskId), any(UserPrincipalDto.class)))
                .willReturn(response);

            mockMvc.perform(
                    patch("/api/projects/{projectId}/tasks/{taskId}/approve", projectId, taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.approvedCount").value(1))
                .andExpect(jsonPath("$.requiredReviewerCount").value(2));
        }

        @Test
        @DisplayName("할 일 재검토 요청 성공 - 200 OK")
        void requestReReview_success() throws Exception {
            mockMvc.perform(
                    post("/api/projects/{projectId}/tasks/{taskId}/re-review", Fixtures.id(),
                        Fixtures.id()))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("할 일 개수 조회 (Count 관련)")
    class CountQuery {

        @Test
        @DisplayName("내 할 일 상태별 개수 조회 성공 - 200 OK")
        void getMyTaskStatusCount_success() throws Exception {
            UUID memberId = Fixtures.id();
            MyTaskStatusCountResponseDto response =
                new MyTaskStatusCountResponseDto(memberId, 5, 3, 2, 7);

            given(taskService.countMyTasksByStatus(any(), any(UserPrincipalDto.class)))
                .willReturn(response);

            mockMvc.perform(get("/api/me/tasks/status-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.done").value(7));
        }

        @Test
        @DisplayName("프로젝트 상태별 할 일 개수 조회 성공 - 200 OK")
        void getProjectTaskStatusCount_success() throws Exception {
            UUID projectId = Fixtures.id();
            ProjectTaskStatusCountResponseDto response =
                new ProjectTaskStatusCountResponseDto(projectId, 5, 3, 2, 7);

            given(taskService.countTasksByStatusForProject(eq(projectId), any(),
                any(UserPrincipalDto.class)))
                .willReturn(response);

            mockMvc.perform(get("/api/projects/{projectId}/tasks/status-count", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.todo").value(5));
        }

        @Test
        @DisplayName("프로젝트 멤버별 상태별 할 일 개수 조회 성공 - 200 OK")
        void getMemberTaskStatusCount_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID memberId = Fixtures.id();

            List<MemberTaskStatusCountResponseDto> response =
                List.of(new MemberTaskStatusCountResponseDto(projectId, memberId, 4, 3, 2));

            given(taskService.countTasksByStatusForAllMembers(eq(projectId), any(),
                any(UserPrincipalDto.class)))
                .willReturn(response);

            mockMvc.perform(get("/api/projects/{projectId}/tasks/members/status-count", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(memberId.toString()))
                .andExpect(jsonPath("$[0].todo").value(4));
        }
    }

    static class Fixtures {

        static final LocalDate DUE = LocalDate.of(2025, 9, 18);

        static UUID id() {
            return UUID.randomUUID();
        }

        static TaskCreateRequestDto reqCreate(
            String title, String desc, TaskStatus status,
            LocalDate due, boolean urgent, int reviewers,
            List<UUID> tags, List<UUID> assignees
        ) {
            return new TaskCreateRequestDto(title, desc, status, due, urgent, reviewers, tags,
                assignees);
        }

        static TaskUpdateRequestDto reqUpdate(
            String title, String desc, TaskStatus status,
            LocalDate due, boolean urgent, int reviewers,
            List<UUID> tags, List<UUID> assignees
        ) {
            return new TaskUpdateRequestDto(title, desc, status, due, urgent, reviewers, tags,
                assignees);
        }

        static TaskStatusRequestDto reqStatus(TaskStatus status) {
            return new TaskStatusRequestDto(status);
        }

        static MemberResponseDto memberDto(UUID id, String name) {
            return new MemberResponseDto(
                id, name, "avatar.png", "#FF5733", true,
                LocalDateTime.of(2025, 9, 5, 15, 0),
                LocalDateTime.of(2025, 9, 5, 16, 0)
            );
        }

        static TaskResponseDto taskResponse(
            UUID taskId, UUID projectId, String title, String description,
            TaskStatus status, boolean urgent, int requiredReviewerCount,
            List<TagResponseDto> tags, List<MemberResponseDto> assignees
        ) {
            return new TaskResponseDto(
                taskId, projectId, title, description, status,
                DUE, urgent, requiredReviewerCount,
                0, 0, tags, assignees,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
            );
        }

        static TaskDetailResponseDto taskDetail(
            UUID id, String title, String description, String status,
            LocalDate due, boolean urgent, int approvedCount,
            int requiredReviewerCount, boolean approvedByMe,
            List<TagResponseDto> tags, List<MemberResponseDto> assignees
        ) {
            return new TaskDetailResponseDto(
                id, title, description, status, due, urgent,
                approvedCount, requiredReviewerCount, approvedByMe,
                tags, assignees, List.of(),
                LocalDateTime.now().minusDays(1), LocalDateTime.now()
            );
        }

        static TaskStatusSectionDto statusSection(
            List<TaskResponseDto> tasks, int count, UUID nextCursor, boolean hasNext
        ) {
            return new TaskStatusSectionDto(tasks, count, nextCursor, hasNext);
        }

        static TaskMemberSectionResponseDto memberSection(
            MemberResponseDto member, List<TaskResponseDto> tasks, int count,
            UUID nextCursor, boolean hasNext
        ) {
            return new TaskMemberSectionResponseDto(member, tasks, count, nextCursor, hasNext);
        }
    }
}
