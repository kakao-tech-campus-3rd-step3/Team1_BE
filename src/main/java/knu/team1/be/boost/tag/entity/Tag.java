package knu.team1.be.boost.tag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.project.entity.Project;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_project_tag_name_deleted_at",
        columnNames = {"project_id", "name", "deleted_at"}
    )
})
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE tags SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class Tag extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public static Tag create(Project project, String name) {
        return Tag.builder()
            .project(project)
            .name(name)
            .build();
    }

    public void update(String name) {
        this.name = name;
    }

    public void ensureTagInProject(UUID projectId) {
        if (!this.project.getId().equals(projectId)) {
            throw new BusinessException(
                ErrorCode.TAG_NOT_IN_PROJECT,
                "tagId=" + this.getId() + ", projectId=" + projectId
            );
        }
    }
}
