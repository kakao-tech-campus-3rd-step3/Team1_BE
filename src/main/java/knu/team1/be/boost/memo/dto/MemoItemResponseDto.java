package knu.team1.be.boost.memo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.memo.entity.Memo;

public record MemoItemResponseDto(
    @Schema(description = "메모 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "메모 제목", example = "메모 제목 1")
    String title,

    @Schema(description = "메모 생성일", example = "2025-10-01T15:30:00")
    LocalDateTime createdAt,

    @Schema(description = "메모 수정일", example = "2025-10-01T15:30:00")
    LocalDateTime updatedAt
) {

    public static MemoItemResponseDto from(Memo memo) {
        return new MemoItemResponseDto(
            memo.getId(),
            memo.getTitle(),
            memo.getCreatedAt(),
            memo.getUpdatedAt()
        );
    }
}
