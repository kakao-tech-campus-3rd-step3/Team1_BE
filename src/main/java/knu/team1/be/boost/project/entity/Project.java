package knu.team1.be.boost.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
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
@Table(name = "project")
public class Project extends SoftDeletableEntity {

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "default_reviewer_count", nullable = false)
    Integer defaultReviewerCount;

    public void updateProject(String name, Integer defaultReviewerCount) {
        if (name != null) {
            this.name = name;
        }
        if (defaultReviewerCount != null) {
            this.defaultReviewerCount = defaultReviewerCount;
        }
    }

}
