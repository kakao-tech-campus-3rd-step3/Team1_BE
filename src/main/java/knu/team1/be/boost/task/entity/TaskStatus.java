package knu.team1.be.boost.task.entity;

public enum TaskStatus {
    TODO,
    PROGRESS,
    REVIEW,
    DONE;

    public int getOrder() {
        return switch (this) {
            case REVIEW -> 1;
            case PROGRESS -> 2;
            case TODO -> 3;
            case DONE -> 99;
        };
    }
}
