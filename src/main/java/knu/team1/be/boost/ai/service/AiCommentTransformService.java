package knu.team1.be.boost.ai.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import knu.team1.be.boost.ai.dto.AiCommentTransformRequestDto;
import knu.team1.be.boost.ai.dto.AiCommentTransformResponseDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiCommentTransformService {

    private final ChatClient chatClient;

    private static final long TIMEOUT_SECONDS = 10L;

    public AiCommentTransformResponseDto transformComment(AiCommentTransformRequestDto requestDto) {
        String originalText = requestDto.text();
        String processedText = preprocessInput(originalText);

        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                this.chatClient.prompt()
                    .user(processedText)
                    .call()
                    .content());

            String transformedText = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            return new AiCommentTransformResponseDto(originalText, transformedText.trim());

        } catch (TimeoutException e) {
            throw new BusinessException(ErrorCode.AI_SERVICE_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    private String preprocessInput(String text) {
        String processed = text.trim();

        // 연속된 공백(스페이스, 탭, 줄바꿈 등)을 하나의 공백으로 정리
        processed = processed.replaceAll("\\s+", " ");

        return processed;
    }
}

