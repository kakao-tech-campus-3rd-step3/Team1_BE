package knu.team1.be.boost.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "회원 전체 서비스 알림 상태 응답 DTO")
public record MemberNotificationResponseDto(

    @Schema(description = "회원 ID", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    UUID memberId,

    @Schema(description = "알림 수신 여부", example = "true")
    boolean enabled
) {

    public static MemberNotificationResponseDto from(UUID memberId, boolean enabled) {
        return new MemberNotificationResponseDto(memberId, enabled);
    }
}
