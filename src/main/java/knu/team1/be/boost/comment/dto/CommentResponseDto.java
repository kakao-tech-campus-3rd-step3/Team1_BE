package knu.team1.be.boost.comment.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.comment.entity.Comment;
import knu.team1.be.boost.comment.entity.Persona;

public record CommentResponseDto(

    UUID commentId,
    UUID authorId,
    String authorName,
    String content,
    Persona persona,
    boolean isAnonymous,
    LocalDateTime createdAt,
    FileInfoResponseDto fileInfo
) {

    public static CommentResponseDto from(Comment comment) {
        // 익명 댓글인 경우 작성자 이름을 "익명"으로 설정
        String authorName = comment.getIsAnonymous()
            ? "익명"
            : comment.getMember().getName();

        return new CommentResponseDto(
            comment.getId(),
            comment.getMember().getId(),
            authorName,
            comment.getContent(),
            comment.getPersona(),
            comment.getIsAnonymous(),
            comment.getCreatedAt(),
            FileInfoResponseDto.from(comment.getFileInfo())
        );
    }
}
