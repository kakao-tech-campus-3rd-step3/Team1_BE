package knu.team1.be.boost.tag.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByProjectIdAndName(UUID projectId, String name);

    List<Tag> findAllByProjectId(UUID projectId);

    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE Tag t SET t.deleted = true, t.deletedAt = CURRENT_TIMESTAMP WHERE t.project.id = :projectId"
    )
    void deleteAllByProjectId(UUID projectId);
}
