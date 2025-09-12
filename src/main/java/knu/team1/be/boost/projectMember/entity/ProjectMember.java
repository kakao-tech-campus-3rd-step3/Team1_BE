package knu.team1.be.boost.projectMember.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE project_member SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Table(name = "project_member", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_project_member",
        columnNames = {"project_id", "member_id"}
    )
})
public class ProjectMember extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ProjectRole role;

    /**
     * 프로젝트와 멤버 간의 연관관계를 나타내는 ProjectMember 엔티티를 생성합니다.
     * 생성된 엔티티는 프로젝트와 멤버의 컬렉션에 자동으로 추가됩니다.
     *
     * @param project 연관 지을 프로젝트
     * @param member  연관 지을 멤버
     * @param role    프로젝트 내에서의 멤버 역할
     * @return 생성된 ProjectMember 엔티티
     */
    public static ProjectMember createProjectMember(
        Project project,
        Member member,
        ProjectRole role
    ) {
        ProjectMember projectMember = ProjectMember.builder()
            .project(project)
            .member(member)
            .role(role)
            .build();

        project.getProjectMembers().add(projectMember);
        member.getProjectMembers().add(projectMember);

        return projectMember;
    }

    public void updateRole(ProjectRole role) {
        this.role = role;
    }

}
