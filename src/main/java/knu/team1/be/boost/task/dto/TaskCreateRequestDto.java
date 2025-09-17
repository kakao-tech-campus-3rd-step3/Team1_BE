package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "할 일 생성 요청 DTO")
public record TaskCreateRequestDto(

    @Schema(description = "할 일 제목", example = "1회차 기술 멘토링 피드백 반영")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 입력할 수 있습니다.")
    String title,

    @Schema(description = "할 일 상세 설명", example = "기술 멘토링에서 나온 멘토님의 피드백을 반영한다.")
    @Size(max = 2000, message = "할 일 상세 설명은 최대 2000자까지 입력할 수 있습니다.")
    String description,

    @Schema(description = "할 일 상태 (TODO/PROGRESS/REVIEW/DONE)", example = "TODO")
    @NotBlank(message = "상태는 필수입니다.")
    String status,

    @Schema(description = "마감일", example = "2025-09-11")
    @NotNull(message = "마감일은 필수입니다.")
    LocalDate dueDate,

    @Schema(description = "긴급 여부", example = "true")
    @NotNull(message = "긴급 여부는 필수입니다.")
    Boolean urgent,

    @Schema(description = "필요 리뷰어 수 (0 이상)", example = "2")
    @NotNull(message = "필요 리뷰어 수는 필수입니다.")
    @Min(value = 0, message = "필요 리뷰어 수는 0 이상이어야 합니다.")
    Integer requiredReviewerCount,

    @Schema(description = "태그 목록", example = "[\"피드백\", \"멘토링\"]")
    List<String> tags,

    @Schema(description = "담당자 UUID 목록", example = "[\"550e8400-e29b-41d4-a716-446655440000\", \"550e8400-e29b-41d4-a716-446655440111\"]")
    List<UUID> assignees
) {

}
