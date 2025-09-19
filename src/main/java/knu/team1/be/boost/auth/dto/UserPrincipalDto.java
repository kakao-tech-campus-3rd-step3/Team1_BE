package knu.team1.be.boost.auth.dto;

import java.util.UUID;

public record UserPrincipalDto(
    UUID id,
    String name,
    String avatar
) {

    public static UserPrincipalDto from(
        UUID id,
        String name,
        String avatar
    ) {
        return new UserPrincipalDto(
            id,
            name,
            avatar
        );
    }
}
