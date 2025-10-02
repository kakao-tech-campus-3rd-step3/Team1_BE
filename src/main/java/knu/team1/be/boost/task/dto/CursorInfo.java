package knu.team1.be.boost.task.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.task.entity.TaskStatus;

public record CursorInfo(
    TaskStatus taskStatus,
    LocalDateTime createdAt,
    LocalDate dueDate,
    UUID taskId
) {

}
