package knu.team1.be.boost.task.entity;

public enum TaskStatus {
    TODO,
    PROGRESS,
    REVIEW,
    DONE;

    public static TaskStatus from(String value) {
        try {
            return TaskStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("존재하지 않는 할 일 상태 값입니다: " + value);
        }
    }
}
