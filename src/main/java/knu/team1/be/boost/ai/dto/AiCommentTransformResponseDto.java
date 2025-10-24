package knu.team1.be.boost.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 댓글 변환 응답 DTO")
public record AiCommentTransformResponseDto(
    @Schema(description = "원본 댓글 내용", example = "이 기능은 좀 별로인 것 같아요.")
    String originalText,

    @Schema(description = "변환된 댓글 내용", example = "이 기능에 대해 개선할 부분이 있을 것 같네요! 😊")
    String transformedText
) {

}

