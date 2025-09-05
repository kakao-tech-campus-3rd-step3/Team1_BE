package knu.team1.be.boost.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import knu.team1.be.boost.entity.BaseEntity;
import knu.team1.be.boost.file.dto.FileRequest;
import knu.team1.be.boost.file.entity.vo.FileMetadata;
import knu.team1.be.boost.file.entity.vo.StorageKey;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.task.entity.Task;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Table(name = "file")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE file SET deleted = true WHERE id = ?")
public class File extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")   // TODO: 인증 붙이면 nullable = false 활성화
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Embedded
    private FileMetadata metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private FileType type;

    @Embedded
    private StorageKey storageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FileStatus status = FileStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static File pendingUpload(FileRequest req, FileType fileType, StorageKey key) {
        return File.builder()
            .member(null) // TODO: 인증 붙이면 세팅
            .task(null)
            .metadata(FileMetadata.of(req.filename(), req.contentType(), req.sizeBytes()))
            .type(fileType)
            .storageKey(key)
            .status(FileStatus.PENDING)
            .completedAt(null)
            .build();
    }

    public boolean isComplete() {
        return this.status == FileStatus.COMPLETED;
    }

    public void complete() {
        if (this.status == FileStatus.COMPLETED) {
            return;
        }
        this.status = FileStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void assignTask(Task task) {
        this.task = task;
    }
}
