package knu.team1.be.boost.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.dto.TaskStatusRequestDto;
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

    private static final UUID PROJECT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
    private static final UUID TASK_ID = UUID.fromString("550e8400-e5f6-7890-1234-567890abcdef");

    private static final UUID TAG1_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440000");
    private static final UUID TAG2_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440111");

    @Nested
    @DisplayName("할 일 생성")
    class CreateTask {

        @Test
        @DisplayName("할 일 생성 성공 - 201 Created + Location 헤더")
        void test1() throws Exception {
            // given
            TaskCreateRequestDto request = new TaskCreateRequestDto(
                "1회차 기술 멘토링 피드백 반영",
                "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                LocalDate.of(2025, 9, 18),
                true,
                2,
                List.of(TAG1_ID, TAG2_ID),
                List.of(
                    UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                    UUID.fromString("550e8400-e29b-41d4-a716-446655440111")
                )
            );

            TaskResponseDto response = new TaskResponseDto(
                TASK_ID,
                PROJECT_ID,
                "1회차 기술 멘토링 피드백 반영",
                "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.",
                TaskStatus.TODO,
                LocalDate.of(2025, 9, 18),
                true,
                2,
                0,
                0,
                List.of(
                    new TagResponseDto(TAG1_ID, "피드백"),
                    new TagResponseDto(TAG2_ID, "멘토링")
                ),
                List.of(
                    new MemberResponseDto(
                        UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                        "김부스트",
                        "avatar1.png",
                        "#FF5733",
                        LocalDateTime.of(2025, 9, 5, 15, 0, 0),
                        LocalDateTime.of(2025, 9, 5, 16, 0, 0)
                    ),
                    new MemberResponseDto(
                        UUID.fromString("550e8400-e29b-41d4-a716-446655440111"),
                        "이부스트",
                        "avatar2.png",
                        "#FF5733",
                        LocalDateTime.of(2025, 9, 6, 10, 0, 0),
                        LocalDateTime.of(2025, 9, 6, 11, 0, 0)
                    )
                ),
                LocalDateTime.of(2025, 9, 17, 12, 0, 0),
                LocalDateTime.of(2025, 9, 17, 12, 0, 0)
            );

            given(
                taskService.createTask(
                    eq(PROJECT_ID), any(TaskCreateRequestDto.class), any(UserPrincipalDto.class)
                )
            ).willReturn(response);

            // when & then
            mockMvc.perform(
                    post("/api/projects/{projectId}/tasks", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                    "/api/projects/" + PROJECT_ID + "/tasks/" + TASK_ID))
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID.toString()))
                .andExpect(jsonPath("$.taskId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.tags[0].tagId").value(TAG1_ID.toString()))
                .andExpect(jsonPath("$.tags[1].tagId").value(TAG2_ID.toString()));
        }

        @Test
        @DisplayName("할 일 생성 실패 - 400 (빈 제목)")
        void test2() throws Exception {
            // given
            TaskCreateRequestDto request = new TaskCreateRequestDto(
                "",
                "설명",
                TaskStatus.TODO,
                LocalDate.of(2025, 9, 18),
                false,
                1,
                List.of(TAG1_ID),
                List.of()
            );

            // when & then
            mockMvc.perform(
                    post("/api/projects/{projectId}/tasks", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("할 일 생성 실패 - 400 (마감일 null)")
        void test3() throws Exception {
            // given
            TaskCreateRequestDto request = new TaskCreateRequestDto(
                "제목",
                "설명",
                TaskStatus.TODO,
                null,
                false,
                1,
                List.of(TAG1_ID),
                List.of()
            );

            // when & then
            mockMvc.perform(
                    post("/api/projects/{projectId}/tasks", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("할 일 생성 실패 - 400 (requiredReviewerCount < 0)")
        void test4() throws Exception {
            // given
            TaskCreateRequestDto request = new TaskCreateRequestDto(
                "제목",
                "설명",
                TaskStatus.TODO,
                LocalDate.of(2025, 9, 18),
                false,
                -1,
                List.of(TAG1_ID),
                List.of()
            );

            // when & then
            mockMvc.perform(
                    post("/api/projects/{projectId}/tasks", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("할 일 생성 실패 - 400 (상태 빈 값)")
        void test5() throws Exception {
            // given
            TaskCreateRequestDto request = new TaskCreateRequestDto(
                "제목",
                "설명",
                null,
                LocalDate.of(2025, 9, 18),
                false,
                1,
                List.of(TAG1_ID),
                List.of()
            );

            // when & then
            mockMvc.perform(
                    post("/api/projects/{projectId}/tasks", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("할 일 수정")
    class UpdateTask {

        @Test
        @DisplayName("할 일 수정 성공 - 200 OK")
        void test1() throws Exception {
            // given
            TaskUpdateRequestDto request = new TaskUpdateRequestDto(
                "수정된 제목",
                "수정된 설명",
                TaskStatus.REVIEW,
                LocalDate.of(2025, 9, 18),
                false,
                1,
                List.of(TAG2_ID),
                List.of()
            );

            TaskResponseDto response = new TaskResponseDto(
                TASK_ID,
                PROJECT_ID,
                "수정된 제목",
                "수정된 설명",
                TaskStatus.REVIEW,
                LocalDate.of(2025, 9, 18),
                false,
                1,
                0,
                0,
                List.of(new TagResponseDto(TAG2_ID, "멘토링")),
                List.of(),
                LocalDateTime.of(2025, 9, 17, 12, 0, 0),
                LocalDateTime.of(2025, 9, 17, 13, 0, 0)
            );

            given(
                taskService.updateTask(
                    eq(PROJECT_ID), eq(TASK_ID), any(TaskUpdateRequestDto.class),
                    any(UserPrincipalDto.class)
                )
            ).willReturn(response);

            // when & then
            mockMvc.perform(
                    put("/api/projects/{projectId}/tasks/{taskId}", PROJECT_ID, TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID.toString()))
                .andExpect(jsonPath("$.taskId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.tags[0].tagId").value(TAG2_ID.toString()));
        }

        @Test
        @DisplayName("할 일 수정 실패 - 400 (빈 제목)")
        void test2() throws Exception {
            // given
            TaskUpdateRequestDto request = new TaskUpdateRequestDto(
                "",
                "설명",
                TaskStatus.PROGRESS,
                LocalDate.of(2025, 9, 18),
                false,
                0,
                List.of(),
                List.of()
            );

            // when & then
            mockMvc.perform(
                    put("/api/projects/{projectId}/tasks/{taskId}", PROJECT_ID, TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("할 일 수정 실패 - 400 (마감일 null)")
        void test3() throws Exception {
            // given
            TaskUpdateRequestDto request = new TaskUpdateRequestDto(
                "제목",
                "설명",
                TaskStatus.PROGRESS,
                null,
                false,
                0,
                List.of(),
                List.of()
            );

            // when & then
            mockMvc.perform(
                    put("/api/projects/{projectId}/tasks/{taskId}", PROJECT_ID, TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("할 일 삭제")
    class DeleteTask {

        @Test
        @DisplayName("할 일 삭제 성공 - 204 No Content")
        void test1() throws Exception {
            // when & then
            mockMvc.perform(
                    delete("/api/projects/{projectId}/tasks/{taskId}", PROJECT_ID, TASK_ID)
                )
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("할 일 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("할 일 상태 변경 성공 - 200 OK")
        void test1() throws Exception {
            // given
            TaskStatusRequestDto request = new TaskStatusRequestDto(TaskStatus.REVIEW);

            TaskResponseDto response = new TaskResponseDto(
                TASK_ID, PROJECT_ID,
                "제목", "설명",
                TaskStatus.REVIEW,
                LocalDate.of(2025, 9, 18),
                true, 2,
                0,
                0,
                List.of(new TagResponseDto(TAG1_ID, "피드백"), new TagResponseDto(TAG2_ID, "멘토링")),
                List.of(),
                LocalDateTime.of(2025, 9, 17, 12, 0, 0),
                LocalDateTime.of(2025, 9, 17, 13, 0, 0)
            );

            given(
                taskService.changeTaskStatus(
                    eq(PROJECT_ID), eq(TASK_ID), any(TaskStatusRequestDto.class),
                    any(UserPrincipalDto.class)
                )
            ).willReturn(response);

            // when & then
            mockMvc.perform(
                    patch("/api/projects/{projectId}/tasks/{taskId}", PROJECT_ID, TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID.toString()))
                .andExpect(jsonPath("$.taskId").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.status").value("REVIEW"))
                .andExpect(jsonPath("$.tags[0].tagId").value(TAG1_ID.toString()))
                .andExpect(jsonPath("$.tags[1].tagId").value(TAG2_ID.toString()));
        }

        @Test
        @DisplayName("실패 - 400 (상태 빈 값)")
        void changeStatus_badRequest_blankStatus() throws Exception {
            // given
            TaskStatusRequestDto request = new TaskStatusRequestDto(null);

            // when & then
            mockMvc.perform(
                    patch("/api/projects/{projectId}/tasks/{taskId}", PROJECT_ID, TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }
    }
}
