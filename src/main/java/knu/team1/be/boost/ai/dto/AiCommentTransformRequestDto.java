package knu.team1.be.boost.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 댓글 변환 요청 DTO")
public record AiCommentTransformRequestDto(
    @Schema(description = "변환할 댓글 내용", example = "이 기능은 좀 별로인 것 같아요.")
    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    @Size(max = 500, message = "댓글은 최대 500자까지 입력할 수 있습니다.")
    String text
) {

}

