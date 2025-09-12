package knu.team1.be.boost.member.exception;

import java.util.List;
import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(UUID userId) {
        super("해당 사용자를 찾을 수 없습니다. id=" + userId);
    }

    public MemberNotFoundException(List<UUID> userIds) {
        super("해당 사용자를 찾을 수 없습니다. id=" + userIds);
    }
}
