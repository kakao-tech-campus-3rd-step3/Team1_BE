package knu.team1.be.boost.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 수정 요청 DTO (PUT)")
public record CommentUpdateRequestDto(
    @Schema(description = "수정할 댓글 내용", example = "아, 이제 이해됐어요. 감사합니다!")
    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    String content,

    @Schema(description = "익명 여부", example = "false")
    Boolean isAnonymous,

    @Schema(description = "첨부 파일 정보")
    FileInfoRequestDto fileInfo
) {

}
