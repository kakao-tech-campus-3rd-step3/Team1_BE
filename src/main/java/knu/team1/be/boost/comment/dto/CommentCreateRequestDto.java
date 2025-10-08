package knu.team1.be.boost.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import knu.team1.be.boost.comment.entity.Persona;

@Schema(description = "댓글 생성 요청 DTO")
public record CommentCreateRequestDto(
    @Schema(description = "댓글 내용", example = "이 부분 설명이 더 필요해요.")
    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    String content,

    @Schema(description = "페르소나", example = "BOO")
    Persona persona,

    @Schema(description = "익명 여부", example = "false")
    Boolean isAnonymous,

    @Schema(description = "첨부 파일 정보")
    FileInfoRequestDto fileInfo
) {

    @Schema(description = "첨부 파일 요청 정보 DTO")
    public record FileInfoRequestDto(
        @Schema(description = "파일 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        UUID fileId,

        @Schema(description = "파일 페이지", example = "10")
        Integer filePage,

        @Schema(description = "파일 내 댓글 x좌표", example = "255")
        Float fileX,

        @Schema(description = "파일 내 댓글 y좌표", example = "255")
        Float fileY
    ) {

    }
}
