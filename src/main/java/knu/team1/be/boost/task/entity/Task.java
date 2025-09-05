package knu.team1.be.boost.task.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Table(name = "task")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE task SET deleted = true WHERE id = ?")
public class Task extends SoftDeletableEntity {

    @Id
    @GeneratedValue
    private UUID id;

}
