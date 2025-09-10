package knu.team1.be.boost.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
import knu.team1.be.boost.projectMember.entity.ProjectMember;
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
@SQLDelete(sql = "UPDATE project SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Table(name = "projects")
public class Project extends SoftDeletableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "default_reviewer_count", nullable = false)
    private Integer defaultReviewerCount;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<ProjectMember> projectMembers = new ArrayList<>();

    public void updateProject(String name, Integer defaultReviewerCount) {
        if (name != null) {
            this.name = name;
        }
        if (defaultReviewerCount != null) {
            this.defaultReviewerCount = defaultReviewerCount;
        }
    }

}
