package knu.team1.be.boost.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import knu.team1.be.boost.ai.dto.AiCommentTransformRequestDto;
import knu.team1.be.boost.ai.dto.AiCommentTransformResponseDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiCommentTransformServiceTest {

    @InjectMocks
    private AiCommentTransformService aiCommentTransformService;

    @Mock
    private ChatClient chatClient;

    @Mock
    private Executor aiTaskExecutor;

    @Mock
    private ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private CallResponseSpec callResponseSpec;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiCommentTransformService, "timeoutSeconds", 10L);
    }

    @Test
    @DisplayName("AI 댓글 변환 성공")
    void transformComment_Success() {
        // given
        String originalText = "이 기능은 좀 별로인 것 같아요.";
        String transformedText = "이 기능에 대해 개선할 부분이 있을 것 같네요! 😊";
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(originalText);

        // 동기적으로 실행되도록 설정
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(aiTaskExecutor).execute(any(Runnable.class));

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(transformedText);

        // when
        AiCommentTransformResponseDto result = aiCommentTransformService.transformComment(
            requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.originalText()).isEqualTo(originalText);
        assertThat(result.transformedText()).isEqualTo(transformedText);
    }

    @Test
    @DisplayName("AI 댓글 변환 성공 - 공백 제거 및 정리")
    void transformComment_Success_WithWhitespace() {
        // given
        String originalText = "  이 기능은    좀  별로인 \n\n  것 같아요.  ";
        String expectedProcessed = "이 기능은 좀 별로인 것 같아요.";
        String transformedText = "이 기능에 대해 개선할 부분이 있을 것 같네요! 😊  "; // 뒤 공백 포함
        String expectedTransformed = "이 기능에 대해 개선할 부분이 있을 것 같네요! 😊";
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(originalText);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(aiTaskExecutor).execute(any(Runnable.class));

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(expectedProcessed)).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(transformedText);

        // when
        AiCommentTransformResponseDto result = aiCommentTransformService.transformComment(
            requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.originalText()).isEqualTo(originalText);
        assertThat(result.transformedText()).isEqualTo(expectedTransformed);
    }

    @Test
    @DisplayName("AI 서비스 일반 예외 발생")
    void transformComment_ServiceError() {
        // given
        String originalText = "테스트 댓글";
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(originalText);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(aiTaskExecutor).execute(any(Runnable.class));

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenThrow(new RuntimeException("AI 서비스 오류"));

        // when & then
        assertThatThrownBy(() -> aiCommentTransformService.transformComment(requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AI_SERVICE_ERROR);
    }

    @Test
    @DisplayName("AI 서비스 인터럽트 예외 발생")
    void transformComment_InterruptedException() {
        // given
        String originalText = "테스트 댓글";
        AiCommentTransformRequestDto requestDto = new AiCommentTransformRequestDto(originalText);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(aiTaskExecutor).execute(any(Runnable.class));

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenAnswer(invocation -> {
            Thread.currentThread().interrupt();
            throw new InterruptedException("인터럽트 발생");
        });

        // when & then
        assertThatThrownBy(() -> aiCommentTransformService.transformComment(requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AI_SERVICE_ERROR);

        assertThat(Thread.interrupted()).isTrue();
    }
}

