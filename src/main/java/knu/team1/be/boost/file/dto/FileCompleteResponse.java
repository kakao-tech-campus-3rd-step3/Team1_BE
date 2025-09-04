package knu.team1.be.boost.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;

@Schema(description = "파일 업로드 완료 응답 DTO")
public record FileCompleteResponse(

    @Schema(description = "파일 ID (UUID 문자열)", example = "2f8f2a2e-4a63-4f3a-8d1b-2a4de6d6f8aa")
    String fileId,

    @Schema(description = "업로드 작업 ID (UUID 문자열)", example = "c8b0a2a7-1a89-4c5c-b4d2-3c9c6c59a1d4")
    String taskId,

    @Schema(description = "파일 상태", example = "completed")
    String status,

    @Schema(description = "완료 시각", type = "string", format = "date-time", example = "2025-09-05T12:34:56")
    LocalDateTime completedAt
) {

    public static FileCompleteResponse from(File file, UUID taskId) {
        return new FileCompleteResponse(
            file.getId().toString(),
            taskId.toString(),
            file.getStatus().name().toLowerCase(),
            file.getCompletedAt()
        );
    }
}
