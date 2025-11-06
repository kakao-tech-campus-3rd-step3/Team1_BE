package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "재검토 요청 시각 응답 DTO")
public record TaskReReviewResponseDto(

    @Schema(description = "재검토 요청 시각", example = "2025-11-06T15:32:10")
    LocalDateTime reReviewRequestedAt
) {

    public static TaskReReviewResponseDto from(LocalDateTime requestedAt) {
        return new TaskReReviewResponseDto(requestedAt);
    }
}
