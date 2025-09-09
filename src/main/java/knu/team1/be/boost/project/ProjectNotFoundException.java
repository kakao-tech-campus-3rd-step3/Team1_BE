package knu.team1.be.boost.project;

import java.util.UUID;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(UUID projectId) {
        super("해당 프로젝트를 찾을 수 없습니다. id=" + projectId);
    }
}
