package knu.team1.be.boost.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.project.dto.ProjectCreateRequestDto;
import knu.team1.be.boost.project.dto.ProjectResponseDto;
import knu.team1.be.boost.project.dto.ProjectUpdateRequestDto;
import knu.team1.be.boost.project.service.ProjectService;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
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
    controllers = ProjectController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    private static final UUID testUserId = TestSecurityConfig.testUserPrincipal.id();
    private final UUID projectId = UUID.randomUUID();

    @TestConfiguration
    static class TestSecurityConfig implements WebMvcConfigurer {

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

    private ProjectResponseDto createMockProjectResponseDto(UUID id, String name) {
        return new ProjectResponseDto(
            id,
            name,
            2,
            ProjectRole.OWNER,
            true
        );
    }

    @Test
    @DisplayName("프로젝트 생성 성공")
    void createProject_Success() throws Exception {
        // given
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto("새 프로젝트");
        ProjectResponseDto responseDto = createMockProjectResponseDto(projectId, "새 프로젝트");

        when(projectService.createProject(any(ProjectCreateRequestDto.class), eq(testUserId)))
            .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(projectId.toString()))
            .andExpect(jsonPath("$.name").value("새 프로젝트"))
            .andExpect(jsonPath("$.role").value("OWNER"))
            .andDo(print());

        verify(projectService, times(1))
            .createProject(any(ProjectCreateRequestDto.class), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - Validation Error (이름 없음)")
    void createProject_Fail_EmptyName() throws Exception {
        // given
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto("");

        // when & then
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(projectService, never()).createProject(any(), any());
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - Validation Error (이름 너무 김)")
    void createProject_Fail_NameTooLong() throws Exception {
        // given
        String longName = "a".repeat(31);
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto(longName);

        // when & then
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(projectService, never()).createProject(any(), any());
    }

    @Test
    @DisplayName("프로젝트 조회 성공")
    void getProject_Success() throws Exception {
        // given
        ProjectResponseDto responseDto = createMockProjectResponseDto(projectId, "테스트 프로젝트");

        when(projectService.getProject(projectId, testUserId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(projectId.toString()))
            .andExpect(jsonPath("$.name").value("테스트 프로젝트"))
            .andDo(print());

        verify(projectService, times(1)).getProject(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 조회 실패 - 프로젝트 없음")
    void getProject_Fail_ProjectNotFound() throws Exception {
        // given
        when(projectService.getProject(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andDo(print());

        verify(projectService, times(1)).getProject(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 조회 실패 - 권한 없음")
    void getProject_Fail_NoPermission() throws Exception {
        // given
        when(projectService.getProject(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectService, times(1)).getProject(projectId, testUserId);
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 성공")
    void getMyProjects_Success() throws Exception {
        // given
        List<ProjectResponseDto> projects = List.of(
            createMockProjectResponseDto(UUID.randomUUID(), "프로젝트1"),
            createMockProjectResponseDto(UUID.randomUUID(), "프로젝트2")
        );

        when(projectService.getMyProjects(testUserId)).thenReturn(projects);

        // when & then
        mockMvc.perform(get("/api/projects/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].name").value("프로젝트1"))
            .andExpect(jsonPath("$[1].name").value("프로젝트2"))
            .andDo(print());

        verify(projectService, times(1)).getMyProjects(testUserId);
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 - 빈 리스트")
    void getMyProjects_EmptyList() throws Exception {
        // given
        when(projectService.getMyProjects(testUserId)).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/projects/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(0))
            .andDo(print());

        verify(projectService, times(1)).getMyProjects(testUserId);
    }

    @Test
    @DisplayName("프로젝트 수정 성공")
    void updateProject_Success() throws Exception {
        // given
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto("수정된 프로젝트", 3);
        ProjectResponseDto responseDto = new ProjectResponseDto(
            projectId,
            "수정된 프로젝트",
            3,
            ProjectRole.OWNER,
            true
        );

        when(projectService.updateProject(eq(projectId), any(ProjectUpdateRequestDto.class),
            eq(testUserId)))
            .thenReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(projectId.toString()))
            .andExpect(jsonPath("$.name").value("수정된 프로젝트"))
            .andExpect(jsonPath("$.defaultReviewerCount").value(3))
            .andDo(print());

        verify(projectService, times(1))
            .updateProject(eq(projectId), any(ProjectUpdateRequestDto.class), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - Validation Error")
    void updateProject_Fail_ValidationError() throws Exception {
        // given
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto("", -1);

        // when & then
        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(projectService, never()).updateProject(any(), any(), any());
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 권한 없음")
    void updateProject_Fail_NotOwner() throws Exception {
        // given
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto("수정된 프로젝트", 3);

        when(projectService.updateProject(any(), any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_OWNER_ONLY));

        // when & then
        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectService, times(1))
            .updateProject(eq(projectId), any(ProjectUpdateRequestDto.class), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 삭제 성공")
    void deleteProject_Success() throws Exception {
        // given
        doNothing().when(projectService).deleteProject(projectId, testUserId);

        // when & then
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(print());

        verify(projectService, times(1)).deleteProject(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 프로젝트 없음")
    void deleteProject_Fail_ProjectNotFound() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND))
            .when(projectService).deleteProject(any(), any());

        // when & then
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andDo(print());

        verify(projectService, times(1)).deleteProject(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 권한 없음")
    void deleteProject_Fail_NotOwner() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.PROJECT_OWNER_ONLY))
            .when(projectService).deleteProject(any(), any());

        // when & then
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectService, times(1)).deleteProject(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 멤버 목록 조회 성공")
    void getProjectMembers_Success() throws Exception {
        // given
        MemberResponseDto member1 = new MemberResponseDto(
            UUID.randomUUID(),
            "멤버1",
            "1111",
            "#FF5733",
            true,
            null,
            null
        );

        MemberResponseDto member2 = new MemberResponseDto(
            UUID.randomUUID(),
            "멤버2",
            "2222",
            "#0000FF",
            true,
            null,
            null
        );

        List<MemberResponseDto> members = List.of(member1, member2);

        when(projectService.getProjectMembers(projectId, testUserId)).thenReturn(members);

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].name").value("멤버1"))
            .andExpect(jsonPath("$[1].name").value("멤버2"))
            .andDo(print());

        verify(projectService, times(1)).getProjectMembers(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 멤버 목록 조회 실패 - 프로젝트 없음")
    void getProjectMembers_Fail_ProjectNotFound() throws Exception {
        // given
        when(projectService.getProjectMembers(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andDo(print());

        verify(projectService, times(1)).getProjectMembers(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 멤버 목록 조회 실패 - 권한 없음")
    void getProjectMembers_Fail_NoPermission() throws Exception {
        // given
        when(projectService.getProjectMembers(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectService, times(1)).getProjectMembers(projectId, testUserId);
    }
}

