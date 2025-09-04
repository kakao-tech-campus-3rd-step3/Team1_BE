package knu.team1.be.boost.file.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;

public record FileCompleteResponse(
    String fileId,
    String taskId,
    String status,
    LocalDateTime completedAt
) {

    public static FileCompleteResponse from(File file, UUID taskId) {
        return new FileCompleteResponse(
            file.getId().toString(),
            taskId.toString(),
            file.getStatus().name().toLowerCase(),
            file.getCompletedAt()
        );
    }
}
