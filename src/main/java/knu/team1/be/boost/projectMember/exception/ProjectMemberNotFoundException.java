package knu.team1.be.boost.projectMember.exception;

import java.util.UUID;

public class ProjectMemberNotFoundException extends RuntimeException {

    public ProjectMemberNotFoundException(UUID projectID, UUID memberID) {
        super("프로젝트 " + projectID + "에 " + memberID + "가 속해있지 않습니다.");
    }

}
