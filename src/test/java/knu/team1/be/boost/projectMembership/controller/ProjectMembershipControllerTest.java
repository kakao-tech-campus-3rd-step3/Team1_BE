package knu.team1.be.boost.projectMembership.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinCodeResponseDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinRequestDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinResponseDto;
import knu.team1.be.boost.projectMembership.service.ProjectMembershipService;
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
    controllers = ProjectMembershipController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class ProjectMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectMembershipService projectMembershipService;

    private static final UUID testUserId = TestSecurityConfig.testUserPrincipal.id();
    private final UUID projectId = UUID.randomUUID();
    private final UUID targetMemberId = UUID.randomUUID();

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

    @Test
    @DisplayName("프로젝트 참여 코드 생성 성공")
    void createProjectJoinCode_Success() throws Exception {
        // given
        ProjectJoinCodeResponseDto responseDto = new ProjectJoinCodeResponseDto(
            "A1B2C3",
            LocalDateTime.now().plusDays(7)
        );

        when(projectMembershipService.generateCode(projectId, testUserId))
            .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/join-code", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.joinCode").value("A1B2C3"))
            .andExpect(jsonPath("$.expiresAt").exists())
            .andDo(print());

        verify(projectMembershipService, times(1)).generateCode(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 참여 코드 생성 실패 - 프로젝트 없음")
    void createProjectJoinCode_Fail_ProjectNotFound() throws Exception {
        // given
        when(projectMembershipService.generateCode(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/join-code", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andDo(print());

        verify(projectMembershipService, times(1)).generateCode(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 참여 코드 생성 실패 - 권한 없음")
    void createProjectJoinCode_Fail_NoPermission() throws Exception {
        // given
        when(projectMembershipService.generateCode(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY));

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/join-code", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectMembershipService, times(1)).generateCode(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 참여 코드 조회 성공")
    void getProjectJoinCode_Success() throws Exception {
        // given
        ProjectJoinCodeResponseDto responseDto = new ProjectJoinCodeResponseDto(
            "A1B2C3",
            LocalDateTime.now().plusDays(7)
        );

        when(projectMembershipService.getCode(projectId, testUserId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/join-code", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.joinCode").value("A1B2C3"))
            .andExpect(jsonPath("$.expiresAt").exists())
            .andDo(print());

        verify(projectMembershipService, times(1)).getCode(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 참여 코드 조회 실패 - 권한 없음")
    void getProjectJoinCode_Fail_NoPermission() throws Exception {
        // given
        when(projectMembershipService.getCode(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/join-code", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectMembershipService, times(1)).getCode(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 참여 성공")
    void joinProject_Success() throws Exception {
        // given
        ProjectJoinRequestDto requestDto = new ProjectJoinRequestDto("A1B2C3");
        ProjectJoinResponseDto responseDto = new ProjectJoinResponseDto(projectId);

        when(projectMembershipService.joinProject(any(ProjectJoinRequestDto.class),
            eq(testUserId)))
            .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/projects/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.projectId").value(projectId.toString()))
            .andDo(print());

        verify(projectMembershipService, times(1))
            .joinProject(any(ProjectJoinRequestDto.class), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 참여 실패 - Validation Error (코드 형식 오류)")
    void joinProject_Fail_ValidationError() throws Exception {
        // given
        ProjectJoinRequestDto requestDto = new ProjectJoinRequestDto("");

        // when & then
        mockMvc.perform(post("/api/projects/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(projectMembershipService, never()).joinProject(any(), any());
    }

    @Test
    @DisplayName("프로젝트 참여 실패 - 유효하지 않은 코드")
    void joinProject_Fail_InvalidCode() throws Exception {
        // given
        ProjectJoinRequestDto requestDto = new ProjectJoinRequestDto("XXXXXX");

        when(projectMembershipService.joinProject(any(ProjectJoinRequestDto.class), any()))
            .thenThrow(new BusinessException(ErrorCode.JOIN_CODE_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/projects/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isNotFound())
            .andDo(print());

        verify(projectMembershipService, times(1))
            .joinProject(any(ProjectJoinRequestDto.class), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 참여 실패 - 이미 참여 중")
    void joinProject_Fail_AlreadyMember() throws Exception {
        // given
        ProjectJoinRequestDto requestDto = new ProjectJoinRequestDto("A1B2C3");

        when(projectMembershipService.joinProject(any(ProjectJoinRequestDto.class), any()))
            .thenThrow(new BusinessException(ErrorCode.MEMBER_ALREADY_JOINED));

        // when & then
        mockMvc.perform(post("/api/projects/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isConflict())
            .andDo(print());

        verify(projectMembershipService, times(1))
            .joinProject(any(ProjectJoinRequestDto.class), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 나가기 성공")
    void leaveProject_Success() throws Exception {
        // given
        doNothing().when(projectMembershipService).leaveProject(projectId, testUserId);

        // when & then
        mockMvc.perform(delete("/api/projects/{projectId}/leave", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(print());

        verify(projectMembershipService, times(1)).leaveProject(projectId, testUserId);
    }

    @Test
    @DisplayName("프로젝트 나가기 실패 - Owner는 나갈 수 없음")
    void leaveProject_Fail_OwnerCannotLeave() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.PROJECT_OWNER_CANNOT_LEAVE))
            .when(projectMembershipService).leaveProject(any(), any());

        // when & then
        mockMvc.perform(delete("/api/projects/{projectId}/leave", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectMembershipService, times(1)).leaveProject(projectId, testUserId);
    }

    @Test
    @DisplayName("멤버 추방 성공")
    void kickMember_Success() throws Exception {
        // given
        doNothing().when(projectMembershipService)
            .kickMember(projectId, targetMemberId, testUserId);

        // when & then
        mockMvc.perform(
                delete("/api/projects/{projectId}/members/{targetMemberId}", projectId,
                    targetMemberId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(print());

        verify(projectMembershipService, times(1))
            .kickMember(projectId, targetMemberId, testUserId);
    }

    @Test
    @DisplayName("멤버 추방 실패 - 권한 없음 (Owner 아님)")
    void kickMember_Fail_NotOwner() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.PROJECT_OWNER_ONLY))
            .when(projectMembershipService).kickMember(any(), any(), any());

        // when & then
        mockMvc.perform(
                delete("/api/projects/{projectId}/members/{targetMemberId}", projectId,
                    targetMemberId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectMembershipService, times(1))
            .kickMember(projectId, targetMemberId, testUserId);
    }

    @Test
    @DisplayName("멤버 추방 실패 - 자기 자신 추방 시도")
    void kickMember_Fail_CannotKickSelf() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.CANNOT_KICK_YOURSELF))
            .when(projectMembershipService).kickMember(any(), any(), any());

        // when & then
        mockMvc.perform(
                delete("/api/projects/{projectId}/members/{targetMemberId}", projectId,
                    targetMemberId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(projectMembershipService, times(1))
            .kickMember(projectId, targetMemberId, testUserId);
    }

    @Test
    @DisplayName("멤버 추방 실패 - Owner 추방 시도")
    void kickMember_Fail_CannotKickOwner() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.CANNOT_KICK_OWNER))
            .when(projectMembershipService).kickMember(any(), any(), any());

        // when & then
        mockMvc.perform(
                delete("/api/projects/{projectId}/members/{targetMemberId}", projectId,
                    targetMemberId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(projectMembershipService, times(1))
            .kickMember(projectId, targetMemberId, testUserId);
    }
}

