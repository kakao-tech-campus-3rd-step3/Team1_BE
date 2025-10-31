package knu.team1.be.boost.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;

public record MemberResponseDto(
    @Schema(description = "회원 고유 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID id,

    @Schema(description = "회원 이름", example = "김부스트")
    String name,

    @Schema(description = "회원 아바타", example = "1111")
    String avatar,

    @Schema(description = "회원 배경색", example = "#FF5733")
    String backgroundColor,

    @Schema(description = "전체 서비스 알림 수신 여부", example = "true")
    boolean notificationEnabled,

    @Schema(description = "계정 생성일", example = "2025-09-05T15:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime createdAt,

    @Schema(description = "마지막 정보 수정일", example = "2025-09-05T16:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime updatedAt
) {

    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
            member.getId(),
            member.getName(),
            member.getAvatar(),
            member.getBackgroundColor(),
            member.isNotificationEnabled(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }
}
