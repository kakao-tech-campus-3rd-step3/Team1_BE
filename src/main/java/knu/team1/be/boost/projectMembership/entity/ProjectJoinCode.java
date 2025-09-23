package knu.team1.be.boost.projectMembership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import knu.team1.be.boost.common.entity.BaseEntity;
import knu.team1.be.boost.project.entity.Project;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project_join_codes")
public class ProjectJoinCode extends BaseEntity {

    @Column(name = "invite_code", nullable = false, unique = true)
    private String inviteCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CodeStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == CodeStatus.ACTIVE && !isExpired();
    }

    public void revoke() {
        this.status = CodeStatus.REVOKED;
    }

    public void updateStatusIfExpired() {
        if (this.status == CodeStatus.ACTIVE && isExpired()) {
            this.status = CodeStatus.EXPIRED;
        }
    }

    public static ProjectJoinCode create(Project project, String inviteCode,
        LocalDateTime expiresAt) {
        return ProjectJoinCode.builder()
            .project(project)
            .inviteCode(inviteCode)
            .status(CodeStatus.ACTIVE)
            .expiresAt(expiresAt)
            .build();
    }
}
