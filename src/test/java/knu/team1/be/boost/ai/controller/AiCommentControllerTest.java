package knu.team1.be.boost.ai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.ai.dto.AiCommentTransformRequestDto;
import knu.team1.be.boost.ai.dto.AiCommentTransformResponseDto;
import knu.team1.be.boost.ai.service.AiCommentTransformService;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
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
    controllers = AiCommentController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class AiCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiCommentTransformService aiCommentTransformService;

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
    @DisplayName("AI 댓글 변환 API 호출 성공")
    void transformComment_Success() throws Exception {
        // given
        String originalText = "이 기능은 좀 별로인 것 같아요.";
        String transformedText = "이 기능에 대해 개선할 부분이 있을 것 같네요! 😊";

        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(originalText);
        AiCommentTransformResponseDto responseDto = new AiCommentTransformResponseDto(
            originalText,
            transformedText
        );

        when(aiCommentTransformService.transformComment(any(AiCommentTransformRequestDto.class)))
            .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/ai/comments/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originalText").value(originalText))
            .andExpect(jsonPath("$.transformedText").value(transformedText))
            .andDo(print());

        verify(aiCommentTransformService, times(1))
            .transformComment(any(AiCommentTransformRequestDto.class));
    }

    @Test
    @DisplayName("AI 댓글 변환 실패 - Validation Error (내용 없음)")
    void transformComment_Fail_EmptyText() throws Exception {
        // given
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(null);

        // when & then
        mockMvc.perform(post("/api/ai/comments/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(aiCommentTransformService, never()).transformComment(any());
    }

    @Test
    @DisplayName("AI 댓글 변환 실패 - Validation Error (빈 문자열)")
    void transformComment_Fail_BlankText() throws Exception {
        // given
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto("   ");

        // when & then
        mockMvc.perform(post("/api/ai/comments/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(aiCommentTransformService, never()).transformComment(any());
    }

    @Test
    @DisplayName("AI 댓글 변환 실패 - Validation Error (500자 초과)")
    void transformComment_Fail_TooLongText() throws Exception {
        // given
        String longText = "a".repeat(501);
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(longText);

        // when & then
        mockMvc.perform(post("/api/ai/comments/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(aiCommentTransformService, never()).transformComment(any());
    }

    @Test
    @DisplayName("AI 댓글 변환 성공 - 최대 길이 (500자)")
    void transformComment_Success_MaxLength() throws Exception {
        // given
        String maxLengthText = "a".repeat(500);
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(maxLengthText);
        AiCommentTransformResponseDto responseDto = new AiCommentTransformResponseDto(
            maxLengthText,
            "변환된 텍스트"
        );

        when(aiCommentTransformService.transformComment(any(AiCommentTransformRequestDto.class)))
            .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/ai/comments/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originalText").value(maxLengthText))
            .andExpect(jsonPath("$.transformedText").value("변환된 텍스트"))
            .andDo(print());

        verify(aiCommentTransformService, times(1))
            .transformComment(any(AiCommentTransformRequestDto.class));
    }

    @Test
    @DisplayName("AI 서비스 타임아웃 예외 발생")
    void transformComment_Timeout() throws Exception {
        // given
        String originalText = "테스트 댓글";
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(originalText);

        when(aiCommentTransformService.transformComment(any(AiCommentTransformRequestDto.class)))
            .thenThrow(new BusinessException(ErrorCode.AI_SERVICE_TIMEOUT));

        // when & then
        mockMvc.perform(post("/api/ai/comments/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isRequestTimeout())
            .andDo(print());

        verify(aiCommentTransformService, times(1))
            .transformComment(any(AiCommentTransformRequestDto.class));
    }

    @Test
    @DisplayName("AI 서비스 일반 오류 발생")
    void transformComment_ServiceError() throws Exception {
        // given
        String originalText = "테스트 댓글";
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(originalText);

        when(aiCommentTransformService.transformComment(any(AiCommentTransformRequestDto.class)))
            .thenThrow(new BusinessException(ErrorCode.AI_SERVICE_ERROR));

        // when & then
        mockMvc.perform(post("/api/ai/comments/transform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isInternalServerError()) // 500
            .andDo(print());

        verify(aiCommentTransformService, times(1))
            .transformComment(any(AiCommentTransformRequestDto.class));
    }
}

