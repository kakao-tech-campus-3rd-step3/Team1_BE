package knu.team1.be.boost.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "프로젝트별 알림 상태 응답 DTO")
public record ProjectNotificationResponseDto(

    @Schema(description = "프로젝트 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID projectId,

    @Schema(description = "회원 ID", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    UUID memberId,

    @Schema(description = "알림 수신 여부", example = "true")
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
