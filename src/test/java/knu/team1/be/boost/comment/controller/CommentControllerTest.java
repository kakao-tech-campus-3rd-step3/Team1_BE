package knu.team1.be.boost.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.comment.dto.CommentCreateRequestDto;
import knu.team1.be.boost.comment.dto.CommentResponseDto;
import knu.team1.be.boost.comment.dto.CommentUpdateRequestDto;
import knu.team1.be.boost.comment.dto.FileInfoRequestDto;
import knu.team1.be.boost.comment.entity.Persona;
import knu.team1.be.boost.comment.service.CommentService;
import knu.team1.be.boost.security.filter.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(
    controllers = CommentController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private static final UUID testUserId = TestSecurityConfig.testUserPrincipal.id();

    private static final String testBackgroundColor = "#FF5733";

    private final UUID projectId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private final UUID commentId = UUID.randomUUID();
    private final UUID fileId = UUID.randomUUID();

    @TestConfiguration
    static class TestSecurityConfig implements WebMvcConfigurer {

        // 테스트 시 사용할 User ID, Name, Avatar 정의
        static UserPrincipalDto testUserPrincipal = new UserPrincipalDto(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "테스트유저",
            "1111"
        );

        @Bean
        public HandlerMethodArgumentResolver authenticationPrincipalArgumentResolver() {
            return new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(UserPrincipalDto.class) &&
                        parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                }

                @Override
                public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
                ) {
                    return testUserPrincipal;
                }
            };
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(authenticationPrincipalArgumentResolver());
        }
    }


    // DTO 스텁을 생성하는 헬퍼 메소드
    private CommentResponseDto createMockResponseDto() {
        CommentResponseDto.AuthorInfoResponseDto author = new CommentResponseDto.AuthorInfoResponseDto(
            testUserId,
            TestSecurityConfig.testUserPrincipal.name(),
            TestSecurityConfig.testUserPrincipal.avatar(),
            testBackgroundColor
        );
        return new CommentResponseDto(
            commentId, author, "테스트 댓글", Persona.BOO, false, null, LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("getComments: 댓글 목록 조회 API 호출 성공")
    void getComments_Success() throws Exception {
        // given
        List<CommentResponseDto> comments = List.of(createMockResponseDto());
        when(commentService.findCommentsByTaskId(projectId, testUserId, taskId))
            .thenReturn(comments);

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/tasks/{taskId}/comments", projectId, taskId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].content").value("테스트 댓글"));

        verify(commentService, times(1)).findCommentsByTaskId(
            projectId,
            testUserId,
            taskId
        );
    }

    @Test
    @DisplayName("createComment: 댓글 생성 API 호출 성공")
    void createComment_Success() throws Exception {
        // given
        FileInfoRequestDto fileDto = new FileInfoRequestDto(
            fileId,
            1,
            10f,
            20f
        );
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
            "새 댓글",
            Persona.BOO,
            false,
            fileDto
        );

        CommentResponseDto responseDto = createMockResponseDto();

        when(commentService.createComment(
            eq(projectId), eq(taskId), eq(testUserId), any(CommentCreateRequestDto.class)
        )).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/comments", projectId, taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.commentId").value(commentId.toString()))
            .andExpect(jsonPath("$.content").value("테스트 댓글"));

        verify(commentService, times(1)).createComment(
            eq(projectId),
            eq(taskId),
            eq(testUserId),
            any(CommentCreateRequestDto.class)
        );
    }

    @Test
    @DisplayName("createComment: 댓글 생성 실패 - Validation Error (내용 없음)")
    void createComment_Fail_Validation() throws Exception {
        // given
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
            null,
            Persona.BOO,
            false,
            null
        );

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/tasks/{taskId}/comments", projectId, taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(commentService, never()).createComment(any(), any(), any(), any());
    }

    @Test
    @DisplayName("updateComment: 댓글 수정 API 호출 성공")
    void updateComment_Success() throws Exception {
        // given
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto(
            "수정된 댓글",
            true,
            null
        );
        CommentResponseDto responseDto = createMockResponseDto();

        when(commentService.updateComment(
            eq(commentId), eq(testUserId), any(CommentUpdateRequestDto.class)
        )).thenReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentId").value(commentId.toString()));

        verify(commentService, times(1)).updateComment(
            eq(commentId),
            eq(testUserId),
            any(CommentUpdateRequestDto.class)
        );
    }

    @Test
    @DisplayName("deleteComment: 댓글 삭제 API 호출 성공")
    void deleteComment_Success() throws Exception {
        // given
        doNothing().when(commentService).deleteComment(commentId, testUserId);

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent()); // 204 No Content

        verify(commentService, times(1)).deleteComment(commentId, testUserId);
    }
}

