package knu.team1.be.boost.comment.repository;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTaskId(UUID taskId);
}
