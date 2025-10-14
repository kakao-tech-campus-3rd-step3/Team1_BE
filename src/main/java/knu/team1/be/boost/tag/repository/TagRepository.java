package knu.team1.be.boost.tag.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByProjectIdAndName(UUID projectId, String name);

    List<Tag> findAllByProjectId(UUID projectId);
}
