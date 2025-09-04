package knu.team1.be.boost.file.dto;

import java.time.LocalDateTime;

public record FileCompleteResponse(
    String fileId,
    String taskId,
    String status,
    LocalDateTime completedAt
) {

}
