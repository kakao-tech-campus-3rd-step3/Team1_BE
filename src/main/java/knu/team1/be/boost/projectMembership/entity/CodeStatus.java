package knu.team1.be.boost.projectMembership.entity;

import lombok.Getter;

@Getter
public enum CodeStatus {
    ACTIVE("활성"),
    REVOKED("철회"), // 사용자에 의해 새로운 코드가 생성될 때 기존 코드 REVOKED
    EXPIRED("만료"); // 만료된 상태 코드 발급 후 일정시간이 지나면 만료

    private final String description;

    CodeStatus(String description) {
        this.description = description;
    }
}
