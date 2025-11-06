package knu.team1.be.boost.memo.controller;

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
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.memo.dto.MemoCreateRequestDto;
import knu.team1.be.boost.memo.dto.MemoItemResponseDto;
import knu.team1.be.boost.memo.dto.MemoResponseDto;
import knu.team1.be.boost.memo.dto.MemoUpdateRequestDto;
import knu.team1.be.boost.memo.service.MemoService;
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
    controllers = MemoController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class MemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemoService memoService;

    private static final UUID testUserId = TestSecurityConfig.testUserPrincipal.id();

    private final UUID projectId = UUID.randomUUID();
    private final UUID memoId = UUID.randomUUID();

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
                public Object resolveArgument(MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
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
    private MemoResponseDto createMockResponseDto() {
        return new MemoResponseDto(
            memoId, "테스트 메모", "테스트 내용",
            LocalDateTime.now().minusDays(1), LocalDateTime.now()
        );
    }

    private MemoItemResponseDto createMockItemResponseDto() {
        return new MemoItemResponseDto(
            memoId,
            "테스트 메모",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("createMemo: 메모 생성 API 호출 성공")
    void createMemo_Success() throws Exception {
        // given
        MemoCreateRequestDto requestDto = new MemoCreateRequestDto("새 메모", "새 내용");
        MemoResponseDto responseDto = createMockResponseDto();

        when(memoService.createMemo(
            eq(projectId), eq(testUserId), any(MemoCreateRequestDto.class)
        )).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/memos", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(memoId.toString()))
            .andExpect(jsonPath("$.title").value("테스트 메모"));

        verify(memoService, times(1)).createMemo(eq(projectId), eq(testUserId),
            any(MemoCreateRequestDto.class));
    }

    @Test
    @DisplayName("createMemo: 메모 생성 실패 - Validation Error (제목 없음)")
    void createMemo_Fail_Validation() throws Exception {
        // given
        MemoCreateRequestDto requestDto = new MemoCreateRequestDto(null, "내용만 있음");

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/memos", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(memoService, never()).createMemo(any(), any(), any());
    }

    @Test
    @DisplayName("createMemo: 메모 생성 실패 - Validation Error (내용 없음)")
    void createMemo_Fail_Validation_BlankContent() throws Exception {
        // given
        MemoCreateRequestDto requestDto = new MemoCreateRequestDto("제목만 있음", null);

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/memos", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(memoService, never()).createMemo(any(), any(), any());
    }

    @Test
    @DisplayName("getMemoList: 메모 목록 조회 API 호출 성공")
    void getMemoList_Success() throws Exception {
        // given
        List<MemoItemResponseDto> responseList = List.of(createMockItemResponseDto());
        when(memoService.findMemosByProjectId(projectId, testUserId)).thenReturn(responseList);

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/memos", projectId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].title").value("테스트 메모"));

        verify(memoService, times(1)).findMemosByProjectId(projectId, testUserId);
    }

    @Test
    @DisplayName("getMemo: 메모 단건 조회 API 호출 성공")
    void getMemo_Success() throws Exception {
        // given
        MemoResponseDto responseDto = createMockResponseDto();
        when(memoService.findMemoById(projectId, memoId, testUserId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/memos/{memoId}", projectId, memoId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(memoId.toString()));

        verify(memoService, times(1)).findMemoById(projectId, memoId, testUserId);
    }

    @Test
    @DisplayName("getMemo: 메모 단건 조회 실패 - 메모 없음 (404 Not Found)")
    void getMemo_Fail_MemoNotFound() throws Exception {
        // given
        when(memoService.findMemoById(projectId, memoId, testUserId))
            .thenThrow(new BusinessException(ErrorCode.MEMO_NOT_FOUND, ""));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/memos/{memoId}", projectId, memoId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound()); // GlobalExceptionHandler가 404로 변환
    }

    @Test
    @DisplayName("updateMemo: 메모 수정 API 호출 성공")
    void updateMemo_Success() throws Exception {
        // given
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto("수정된 제목", "수정된 내용");
        MemoResponseDto responseDto = createMockResponseDto();

        when(memoService.updateMemo(
            eq(projectId), eq(memoId), eq(testUserId), any(MemoUpdateRequestDto.class)
        )).thenReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/projects/{projectId}/memos/{memoId}", projectId, memoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(memoId.toString()));

        verify(memoService, times(1)).updateMemo(eq(projectId), eq(memoId), eq(testUserId),
            any(MemoUpdateRequestDto.class));
    }

    @Test
    @DisplayName("updateMemo: 메모 수정 실패 - Validation Error (제목 없음)")
    void updateMemo_Fail_Validation_BlankTitle() throws Exception {
        // given
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(null, "수정된 내용");

        // when & then
        mockMvc.perform(put("/api/projects/{projectId}/memos/{memoId}", projectId, memoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(memoService, never()).updateMemo(any(), any(), any(), any());
    }

    @Test
    @DisplayName("updateMemo: 메모 수정 실패 - Validation Error (내용 없음)")
    void updateMemo_Fail_Validation_BlankContent() throws Exception {
        // given
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto("수정된 제목", null);

        // when & then
        mockMvc.perform(put("/api/projects/{projectId}/memos/{memoId}", projectId, memoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(memoService, never()).updateMemo(any(), any(), any(), any());
    }

    @Test
    @DisplayName("deleteMemo: 메모 삭제 API 호출 성공")
    void deleteMemo_Success() throws Exception {
        // given
        doNothing().when(memoService).deleteMemo(projectId, memoId, testUserId);

        // when & then
        mockMvc.perform(delete("/api/projects/{projectId}/memos/{memoId}", projectId, memoId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent()); // 204 No Content

        verify(memoService, times(1)).deleteMemo(projectId, memoId, testUserId);
    }
}
