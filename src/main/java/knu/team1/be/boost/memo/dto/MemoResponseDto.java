package knu.team1.be.boost.memo.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.memo.entity.Memo;

public record MemoResponseDto(
    UUID id,
    String title,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static MemoResponseDto from(Memo memo) {
        return new MemoResponseDto(
            memo.getId(),
            memo.getTitle(),
            memo.getContent(),
            memo.getCreatedAt(),
            memo.getUpdatedAt()
        );
    }
}
