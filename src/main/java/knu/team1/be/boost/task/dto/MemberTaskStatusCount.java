package knu.team1.be.boost.task.dto;

import java.util.UUID;

public record MemberTaskStatusCount(
    UUID memberId,
    long todo,
    long progress,
    long review
) {

}
