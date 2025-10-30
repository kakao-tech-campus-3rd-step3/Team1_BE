package knu.team1.be.boost.task.dto;

import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.task.entity.Task;

public record TaskApproveEvent(
    Project project,
    Task task
) {

    public static TaskApproveEvent from(Project project, Task task) {
        return new TaskApproveEvent(
            project,
            task
        );
    }
}
