package knu.team1.be.boost.notification.dto;

import java.util.UUID;

public record ProjectNotificationResponseDto(
    UUID projectId,
    UUID memberId,
    boolean enabled
) {

    public static ProjectNotificationResponseDto from(
        UUID projectId,
        UUID memberId,
        boolean enabled
    ) {
        return new ProjectNotificationResponseDto(
            projectId,
            memberId,
            enabled
        );
    }
}
