package knu.team1.be.boost.member.dto;

import java.util.UUID;

public record MemberNotificationResponseDto(
    UUID memberId,
    boolean enabled
) {

    public static MemberNotificationResponseDto from(UUID memberId, boolean enabled) {
        return new MemberNotificationResponseDto(memberId, enabled);
    }
}
