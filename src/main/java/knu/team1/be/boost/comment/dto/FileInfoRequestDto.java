package knu.team1.be.boost.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "첨부 파일 요청 정보 DTO")
public record FileInfoRequestDto(
    @Schema(description = "파일 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @NotNull(message = "파일 ID는 필수입니다.")
    UUID fileId,

    @Schema(description = "파일 페이지", example = "10")
    @NotNull(message = "파일 페이지는 필수입니다.")
    Integer filePage,

    @Schema(description = "파일 내 댓글 x좌표", example = "255")
    @NotNull(message = "파일 좌표는 필수입니다.")
    Float fileX,

    @Schema(description = "파일 내 댓글 y좌표", example = "255")
    @NotNull(message = "파일 좌표는 필수입니다.")
    Float fileY
) {

}
