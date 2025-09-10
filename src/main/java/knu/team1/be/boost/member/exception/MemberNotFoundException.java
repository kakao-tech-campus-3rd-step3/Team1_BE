package knu.team1.be.boost.member.exception;

import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(UUID userId) {
        super("해당 사용자를 찾을 수 없습니다. id=" + userId);
    }
}
