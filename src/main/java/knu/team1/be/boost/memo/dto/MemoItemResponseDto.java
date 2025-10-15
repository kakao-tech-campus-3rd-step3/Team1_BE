package knu.team1.be.boost.memo.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.memo.entity.Memo;

public record MemoItemResponseDto(
    UUID id,
    String title,
    LocalDateTime createdAt,
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
