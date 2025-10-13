package knu.team1.be.boost.tag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
import knu.team1.be.boost.project.entity.Project;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(name = "uk_project_tag_name", columnNames = {"project_id", "name"})
})
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE tags SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Tag extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 50)
    private String name;

}
