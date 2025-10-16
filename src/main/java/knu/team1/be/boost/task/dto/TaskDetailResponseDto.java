package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.comment.dto.CommentResponseDto;
import knu.team1.be.boost.comment.entity.Comment;
import knu.team1.be.boost.file.dto.FileResponseDto;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.tag.dto.TagResponseDto;
import knu.team1.be.boost.task.entity.Task;

@Schema(description = "할 일(Task) 상세 응답 DTO")
public record TaskDetailResponseDto(

    @Schema(description = "할 일 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "할 일 제목", example = "1회차 기술 멘토링 피드백 반영")
    String title,

    @Schema(description = "할 일 상세 설명", example = "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.")
    String description,

    @Schema(description = "할 일 상태 (TODO/PROGRESS/REVIEW/DONE)", example = "TODO")
    String status,

    @Schema(description = "마감일", example = "2025-09-11")
    LocalDate dueDate,

    @Schema(description = "긴급 여부", example = "true")
    Boolean urgent,

    @Schema(description = "현재 승인된 리뷰어 수", example = "1")
    Integer approvedCount,

    @Schema(description = "필요 리뷰어 수", example = "2")
    Integer requiredReviewerCount,

    @Schema(
        description = "태그 목록",
        example = "[{\"tagId\":\"770e8400-e29b-41d4-a716-446655440000\",\"name\":\"피드백\"}," +
            "{\"tagId\":\"770e8400-e29b-41d4-a716-446655440111\",\"name\":\"버그\"}]"
    )
    List<TagResponseDto> tags,

    @Schema(description = "담당자 목록", example = "[{\"id\":\"550e8400-e29b-41d4-a716-446655440000\",\"name\":\"홍길동\"}]")
    List<MemberResponseDto> assignees,

    @Schema(description = "댓글 목록", example = "[{\"id\":\"e2f8c7d0-4b0a-4b6f-9e4a-1c2d3f4a5b6c\",\"content\":\"좋습니다.\",\"authorName\":\"홍길동\"}]")
    List<CommentResponseDto> comments,

    @Schema(description = "첨부 파일 목록", example = "[{\"id\":\"2f8f2a2e-4a63-4f3a-8d1b-2a4de6d6f8aa\",\"filename\":\"피드백.pdf\",\"contentType\":\"application/pdf\",\"sizeBytes\":102400,\"type\":\"PDF\"}]")
    List<FileResponseDto> files,

    @Schema(description = "할 일 생성일", example = "2025-10-01T15:30:00")
    LocalDateTime createdAt,

    @Schema(description = "할 일 수정일", example = "2025-10-02T10:20:00")
    LocalDateTime updatedAt
) {

    public static TaskDetailResponseDto from(
        Task task,
        List<Comment> comments,
        List<File> files,
        List<Member> projectMembers
    ) {
        return new TaskDetailResponseDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().name(),
            task.getDueDate(),
            task.getUrgent(),
            task.getApprovers().size(),
            task.getRequiredApprovalsCount(projectMembers),
            task.getTags().stream()
                .map(TagResponseDto::from)
                .collect(Collectors.toList()),
            task.getAssignees().stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList()),
            comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList()),
            files.stream()
                .map(FileResponseDto::from)
                .collect(Collectors.toList()),
            task.getCreatedAt(),
            LocalDateTime.now()
        );
    }
}
