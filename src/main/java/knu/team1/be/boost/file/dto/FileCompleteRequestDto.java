package knu.team1.be.boost.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "파일 업로드 완료 요청 DTO")
public record FileCompleteRequestDto(

    @Schema(description = "업로드 작업 ID (UUID 문자열)", example = "c8b0a2a7-1a89-4c5c-b4d2-3c9c6c59a1d4")
    @NotBlank(message = "할 일 ID는 필수입니다.")
    String taskId,

    @Schema(description = "파일명", example = "example.pdf")
    @NotBlank(message = "파일명은 필수입니다.")
    String filename,

    @Schema(description = "Content-Type", example = "application/pdf")
    @NotBlank(message = "Content-Type은 필수입니다.")
    String contentType,

    @Schema(description = "파일 크기 (바이트 단위)", example = "1048576")
    @NotNull(message = "파일 크기는 필수입니다.")
    @Positive(message = "파일 크기는 양수이어야 합니다.")
    Integer sizeBytes
) {

}
