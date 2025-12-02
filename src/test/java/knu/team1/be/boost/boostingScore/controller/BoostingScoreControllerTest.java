package knu.team1.be.boost.boostingScore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.boostingScore.dto.BoostingScoreResponseDto;
import knu.team1.be.boost.boostingScore.service.BoostingScoreService;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
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
    controllers = BoostingScoreController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class BoostingScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoostingScoreService boostingScoreService;

    private static final UUID testUserId = TestSecurityConfig.testUserPrincipal.id();

    private final UUID projectId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

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

    private BoostingScoreResponseDto createMockResponseDto(UUID memberId, Integer rank) {
        return BoostingScoreResponseDto.builder()
            .memberId(memberId)
            .totalScore(25)
            .rank(rank)
            .calculatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 성공")
    void getBoostingScores_Success() throws Exception {
        // given
        List<BoostingScoreResponseDto> scores = List.of(
            createMockResponseDto(memberId, 1),
            createMockResponseDto(UUID.randomUUID(), 2),
            createMockResponseDto(UUID.randomUUID(), 3)
        );

        when(boostingScoreService.getProjectBoostingScores(projectId, testUserId))
            .thenReturn(scores);

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/boosting-scores", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(3))
            .andExpect(jsonPath("$[0].rank").value(1))
            .andExpect(jsonPath("$[0].totalScore").value(25))
            .andExpect(jsonPath("$[1].rank").value(2))
            .andExpect(jsonPath("$[2].rank").value(3))
            .andDo(print());

        verify(boostingScoreService, times(1))
            .getProjectBoostingScores(eq(projectId), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 성공 - 빈 리스트")
    void getBoostingScores_Success_EmptyList() throws Exception {
        // given
        when(boostingScoreService.getProjectBoostingScores(projectId, testUserId))
            .thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/boosting-scores", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(0))
            .andDo(print());

        verify(boostingScoreService, times(1))
            .getProjectBoostingScores(eq(projectId), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 실패 - 프로젝트 멤버 아님")
    void getBoostingScores_Fail_NotProjectMember() throws Exception {
        // given
        when(boostingScoreService.getProjectBoostingScores(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/boosting-scores", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(print());

        verify(boostingScoreService, times(1))
            .getProjectBoostingScores(eq(projectId), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 실패 - 프로젝트 없음")
    void getBoostingScores_Fail_ProjectNotFound() throws Exception {
        // given
        when(boostingScoreService.getProjectBoostingScores(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/boosting-scores", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andDo(print());

        verify(boostingScoreService, times(1))
            .getProjectBoostingScores(eq(projectId), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 실패 - 점수 계산 실패")
    void getBoostingScores_Fail_CalculationFailed() throws Exception {
        // given
        when(boostingScoreService.getProjectBoostingScores(any(), any()))
            .thenThrow(new BusinessException(ErrorCode.BOOSTING_SCORE_CALCULATION_FAILED));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/boosting-scores", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andDo(print());

        verify(boostingScoreService, times(1))
            .getProjectBoostingScores(eq(projectId), eq(testUserId));
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 - 단일 멤버")
    void getBoostingScores_SingleMember() throws Exception {
        // given
        List<BoostingScoreResponseDto> scores = List.of(
            createMockResponseDto(memberId, 1)
        );

        when(boostingScoreService.getProjectBoostingScores(projectId, testUserId))
            .thenReturn(scores);

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/boosting-scores", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].rank").value(1))
            .andExpect(jsonPath("$[0].totalScore").value(25))
            .andExpect(jsonPath("$[0].memberId").value(memberId.toString()))
            .andDo(print());

        verify(boostingScoreService, times(1))
            .getProjectBoostingScores(eq(projectId), eq(testUserId));
    }
}

