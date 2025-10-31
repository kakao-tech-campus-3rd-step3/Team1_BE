package knu.team1.be.boost.notification.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.notification.entity.Notification;

@Schema(description = "알림 목록 페이지 응답 DTO")
public record NotificationListResponseDto(

    @ArraySchema(
        schema = @Schema(implementation = NotificationResponseDto.class),
        arraySchema = @Schema(description = "알림 목록")
    )
    List<NotificationResponseDto> notifications,

    @Schema(description = "현재 페이지에 포함된 알림 개수", example = "20")
    int count,

    @Schema(description = "다음 페이지 요청용 커서 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID nextCursor,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext
) {

    public static NotificationListResponseDto from(
        List<Notification> notifications,
        int limit
    ) {
        boolean hasNext = notifications.size() > limit;
        UUID nextCursor = null;

        List<NotificationResponseDto> notificationDtos = notifications.stream()
            .limit(limit)
            .map(NotificationResponseDto::from)
            .toList();

        if (hasNext && !notificationDtos.isEmpty()) {
            nextCursor = notificationDtos.getLast().id();
        }

        return new NotificationListResponseDto(
            notificationDtos,
            notificationDtos.size(),
            nextCursor,
            hasNext
        );
    }
}
