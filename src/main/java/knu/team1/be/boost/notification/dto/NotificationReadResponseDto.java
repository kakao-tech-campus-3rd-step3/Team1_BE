package knu.team1.be.boost.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import knu.team1.be.boost.notification.entity.Notification;

@Schema(description = "알림 읽음 처리 응답 DTO")
public record NotificationReadResponseDto(

    @Schema(description = "알림 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID notificationId,

    @Schema(description = "읽음 여부", example = "true")
    boolean read
) {

    public static NotificationReadResponseDto from(Notification notification) {
        return new NotificationReadResponseDto(
            notification.getId(),
            notification.isRead()
        );
    }
}
