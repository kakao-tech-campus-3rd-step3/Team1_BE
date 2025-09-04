package knu.team1.be.boost.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.user.entity.User;

public record UserResponseDto(
    UUID id,
    String name,
    String profileEmoji,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getName(),
            user.getProfileEmoji(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
