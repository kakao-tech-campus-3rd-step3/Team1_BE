package knu.team1.be.boost.task.exception;

import java.util.UUID;

public class TaskNotInProjectException extends RuntimeException {

    public TaskNotInProjectException(UUID projectId, UUID taskId) {
        super("해당 프로젝트에 속한 할 일이 아닙니다. projectId=" + projectId + ", taskId=" + taskId);
    }
}
