package knu.team1.be.boost.common.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("해당 사용자를 찾을 수 없습니다. id=" + userId);
    }
}
