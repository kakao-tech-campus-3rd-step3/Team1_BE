package knu.team1.be.boost.memo.repository;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.memo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRepository extends JpaRepository<Memo, UUID> {

    List<Memo> findAllByProjectId(UUID projectId);
}
