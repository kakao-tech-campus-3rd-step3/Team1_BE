package knu.team1.be.boost.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import knu.team1.be.boost.comment.entity.Persona;

@Schema(description = "댓글 생성 요청 DTO")
public record CommentCreateRequestDto(
    @Schema(description = "댓글 내용", example = "이 부분 설명이 더 필요해요.")
    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    @Size(max = 1000, message = "댓글은 최대 1000자까지 입력할 수 있습니다.")
    String content,

    @Schema(description = "페르소나", example = "BOO")
    Persona persona,

    @Schema(description = "익명 여부", example = "false")
    Boolean isAnonymous,

    @Schema(description = "첨부 파일 정보")
    FileInfoRequestDto fileInfo
) {


}
