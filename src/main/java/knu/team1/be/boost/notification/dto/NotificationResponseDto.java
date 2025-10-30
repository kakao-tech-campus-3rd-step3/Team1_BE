package knu.team1.be.boost.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.notification.entity.Notification;

@Schema(description = "사용자 알림 응답 DTO")
public record NotificationResponseDto(

    @Schema(description = "알림 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID id,

    @Schema(description = "알림 제목", example = "작업 승인 완료")
    String title,

    @Schema(description = "알림 메시지 내용", example = "[프론트엔드 UI 구현] 작업이 모든 승인자를 통해 승인되었습니다.")
    String message,

    @Schema(description = "알림 읽음 여부", example = "false")
    boolean read,

    @Schema(description = "알림 생성 시간", example = "2025-10-27T08:30:00")
    LocalDateTime createdAt
) {

    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
