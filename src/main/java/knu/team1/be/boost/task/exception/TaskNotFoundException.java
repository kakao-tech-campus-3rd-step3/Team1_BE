package knu.team1.be.boost.task.exception;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(UUID taskId) {
        super("할 일을 찾을 수 없습니다: " + taskId);
    }
}
