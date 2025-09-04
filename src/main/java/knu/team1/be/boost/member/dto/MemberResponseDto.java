package knu.team1.be.boost.member.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;

public record MemberResponseDto(
    UUID id,
    String name,
    String profileEmoji,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
            member.getId(),
            member.getName(),
            member.getProfileEmoji(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }
}
