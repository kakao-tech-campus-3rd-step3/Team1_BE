package knu.team1.be.boost.task.dto;

import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.task.entity.Task;

public record TaskReviewEvent(
    Project project,
    Task task
) {

    public static TaskReviewEvent from(Project project, Task task) {
        return new TaskReviewEvent(
            project,
            task
        );
    }
}
