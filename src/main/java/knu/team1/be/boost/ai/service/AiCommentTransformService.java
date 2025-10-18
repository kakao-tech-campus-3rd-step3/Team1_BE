package knu.team1.be.boost.ai.service;

import knu.team1.be.boost.ai.dto.AiCommentTransformRequestDto;
import knu.team1.be.boost.ai.dto.AiCommentTransformResponseDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCommentTransformService {

    private final ChatClient chatClient;

    public AiCommentTransformResponseDto transformComment(AiCommentTransformRequestDto requestDto) {
        String originalText = requestDto.text();

        try {
            String transformedText = this.chatClient.prompt().user(requestDto.text()).call()
                .content();

            return new AiCommentTransformResponseDto(originalText, transformedText);
        } catch (Exception e) {
            log.error("AI 댓글 변환 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, e.getMessage());
        }
    }
}

