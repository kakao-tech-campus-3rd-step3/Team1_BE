package knu.team1.be.boost.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 개수 응답 DTO")
public record NotificationCountResponseDto(
    @Schema(description = "전체 알림 개수", example = "42")
    long totalCount,

    @Schema(description = "읽지 않은 알림 개수", example = "5")
    long unreadCount
) {

    public static NotificationCountResponseDto from(long total, long unread) {
        return new NotificationCountResponseDto(total, unread);
    }
}
