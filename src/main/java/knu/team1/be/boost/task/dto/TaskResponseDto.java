package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.task.entity.Task;

@Schema(description = "할 일 응답 DTO")
public record TaskResponseDto(

    @Schema(description = "할 일 ID (UUID)", example = "440u8400-e5f6-7890-1234-567890abcdef")
    UUID taskId,

    @Schema(description = "프로젝트 ID (UUID)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID projectId,

    @Schema(description = "할 일 제목", example = "1회차 기술 멘토링 피드백 반영")
    String title,

    @Schema(description = "할 일 상세 설명", example = "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.")
    String description,

    @Schema(description = "할 일 상태 (TODO/PROGRESS/REVIEW/DONE)", example = "TODO")
    String status,

    @Schema(description = "마감일", example = "2025-08-26")
    LocalDate dueDate,

    @Schema(description = "긴급 여부", example = "true")
    Boolean urgent,

    @Schema(description = "필요 리뷰어 수 (0 이상)", example = "2")
    Integer requiredReviewerCount,

    @Schema(description = "태그 목록", example = "[\"피드백\", \"멘토링\"]")
    List<String> tags,

    @Schema(description = "담당자 목록")
    List<MemberResponseDto> assignees,

    @Schema(description = "할 일 생성일", example = "2025-09-12T12:00:00")
    LocalDateTime createdAt,

    @Schema(description = "할 일 수정일", example = "2025-09-12T13:00:00")
    LocalDateTime updatedAt
) {

    public static TaskResponseDto from(Task task) {
        return new TaskResponseDto(
            task.getId(),
            task.getProject().getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().name(),
            task.getDueDate(),
            task.getUrgent(),
            task.getRequiredReviewerCount(),
            task.getTags(),
            task.getAssignees().stream()
                .map(MemberResponseDto::from)
                .toList(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
