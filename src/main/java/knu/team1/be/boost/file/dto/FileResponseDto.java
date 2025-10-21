package knu.team1.be.boost.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;

@Schema(description = "파일 응답 DTO")
public record FileResponseDto(

    @Schema(description = "파일 ID (UUID)", example = "2f8f2a2e-4a63-4f3a-8d1b-2a4de6d6f8aa")
    UUID id,

    @Schema(description = "파일 이름", example = "document.pdf")
    String filename,

    @Schema(description = "파일 MIME 타입", example = "application/pdf")
    String contentType,

    @Schema(description = "파일 크기 (바이트)", example = "102400")
    Integer sizeBytes,

    @Schema(description = "파일 타입", example = "PDF")
    String type,

    @Schema(description = "업로드 완료 시간", example = "2025-10-21T15:30:00")
    LocalDateTime completedAt

) {

    public static FileResponseDto from(File file) {
        return new FileResponseDto(
            file.getId(),
            file.getMetadata().originalFilename(),
            file.getMetadata().contentType(),
            file.getMetadata().sizeBytes(),
            file.getType().name(),
            file.getCompletedAt()
        );
    }
}
