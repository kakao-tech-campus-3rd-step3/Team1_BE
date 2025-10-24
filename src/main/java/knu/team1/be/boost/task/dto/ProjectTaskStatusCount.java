package knu.team1.be.boost.task.dto;

public record ProjectTaskStatusCount(
    long todo,
    long progress,
    long review,
    long done
) {

}
