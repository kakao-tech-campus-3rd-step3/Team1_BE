package knu.team1.be.boost.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.comment.entity.Comment;
import knu.team1.be.boost.comment.entity.Persona;
import knu.team1.be.boost.comment.entity.vo.FileInfo;
import knu.team1.be.boost.member.entity.Member;

@Schema(description = "댓글 응답 DTO")
public record CommentResponseDto(
    @Schema(description = "댓글 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID commentId,

    @Schema(description = "작성자 정보")
    AuthorInfoResponseDto authorInfo,

    @Schema(description = "댓글 내용", example = "이 부분은 수정이 필요할 것 같아요!")
    String content,

    @Schema(description = "페르소나", example = "BOO")
    Persona persona,

    @Schema(description = "익명 여부")
    Boolean isAnonymous,

    @Schema(description = "첨부 파일 정보")
    FileInfoResponseDto fileInfo,

    @Schema(description = "생성 시각", example = "2025-09-05T15:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 시각", example = "2025-09-05T15:00:00")
    LocalDateTime updatedAt
) {

    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
            comment.getId(),
            comment.getIsAnonymous()
                ? AuthorInfoResponseDto.anonymous(comment.getMember())
                : AuthorInfoResponseDto.from(comment.getMember()),
            comment.getContent(),
            comment.getPersona(),
            comment.getIsAnonymous(),
            Optional.ofNullable(comment.getFileInfo())
                .map(FileInfoResponseDto::from)
                .orElse(null),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }

    @Schema(description = "작성자 정보 DTO")
    public record AuthorInfoResponseDto(
        @Schema(description = "회원 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        UUID memberId,

        @Schema(description = "회원 이름", example = "김철수")
        String name,

        @Schema(description = "회원 아바타", example = "1111")
        String avatar,

        @Schema(description = "회원 배경색", example = "#FF5733")
        String backgroundColor
    ) {

        public static AuthorInfoResponseDto from(Member member) {
            return new AuthorInfoResponseDto(
                member.getId(),
                member.getName(),
                member.getAvatar(),
                member.getBackgroundColor()
            );
        }

        public static AuthorInfoResponseDto anonymous(Member member) {
            return new AuthorInfoResponseDto(
                member.getId(),
                "익명",
                "0000",
                "#99a1af"
            );
        }
    }

    @Schema(description = "첨부 파일 응답 정보 DTO")
    public record FileInfoResponseDto(
        @Schema(description = "파일 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        UUID fileId,

        @Schema(description = "파일 페이지", example = "10")
        Integer filePage,

        @Schema(description = "파일 내 댓글 x좌표", example = "255")
        Float fileX,

        @Schema(description = "파일 내 댓글 y좌표", example = "255")
        Float fileY
    ) {

        public static FileInfoResponseDto from(FileInfo fileInfo) {
            return new FileInfoResponseDto(
                fileInfo.getFile().getId(),
                fileInfo.getFilePage(),
                fileInfo.getFileX(),
                fileInfo.getFileY()
            );
        }
    }
}
