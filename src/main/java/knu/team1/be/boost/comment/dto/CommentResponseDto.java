package knu.team1.be.boost.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.comment.entity.Comment;

@Schema(description = "댓글 응답 DTO")
public record CommentResponseDto(

    @Schema(description = "댓글 ID", example = "e2f8c7d0-4b0a-4b6f-9e4a-1c2d3f4a5b6c")
    UUID id,

    @Schema(description = "댓글 내용", example = "좋습니다.")
    String content,

    @Schema(description = "작성자 이름", example = "홍길동")
    String authorName,

    @Schema(description = "작성일시", example = "2025-10-01T15:30:00")
    LocalDateTime createdAt
) {

    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
            comment.getId(),
            comment.getContent(),
            comment.getMember().getName(),
            comment.getCreatedAt()
        );
    }
}
