package knu.team1.be.boost.projectMember.entity;

import lombok.Getter;

@Getter
public enum ProjectRole {
    OWNER("프로젝트 소유자"), MEMBER("일반 멤버");

    private final String description;

    ProjectRole(String description) {
        this.description = description;
    }

}
