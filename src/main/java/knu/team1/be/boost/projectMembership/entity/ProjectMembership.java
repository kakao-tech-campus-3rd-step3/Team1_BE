package knu.team1.be.boost.projectMembership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.project.entity.Project;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE project_membership SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
@Table(name = "project_membership", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_project_member",
        columnNames = {"project_id", "member_id"}
    )
})
public class ProjectMembership extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ProjectRole role;

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private boolean notificationEnabled = true;

    /**
     * 프로젝트와 멤버 간의 연관관계를 나타내는 ProjectMembership 엔티티를 생성합니다. 생성된 엔티티는 프로젝트와 멤버의 컬렉션에 자동으로 추가됩니다.
     *
     * @param project 연관 지을 프로젝트
     * @param member  연관 지을 멤버
     * @param role    프로젝트 내에서의 멤버 역할
     * @return 생성된 ProjectMembership 엔티티
     */
    public static ProjectMembership createProjectMembership(
        Project project,
        Member member,
        boolean notificationEnabled,
        ProjectRole role
    ) {
        ProjectMembership projectMembership = ProjectMembership.builder()
            .project(project)
            .member(member)
            .notificationEnabled(notificationEnabled)
            .role(role)
            .build();

        project.getProjectMemberships().add(projectMembership);
        member.getProjectMemberships().add(projectMembership);

        return projectMembership;
    }

    public static ProjectMembership createProjectMembership(
        Project project,
        Member member,
        ProjectRole role
    ) {
        return createProjectMembership(project, member, true, role);
    }

    public void updateRole(ProjectRole role) {
        this.role = role;
    }

}
