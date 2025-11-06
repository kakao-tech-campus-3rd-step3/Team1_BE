package knu.team1.be.boost.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.tag.entity.Tag;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Table(name = "tasks")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE tasks SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
public class Task extends SoftDeletableEntity {

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "urgent", nullable = false)
    private Boolean urgent;

    @Column(name = "required_reviewer_count", nullable = false)
    private Integer requiredReviewerCount;

    @Column(name = "re_review_requested_at")
    private LocalDateTime reReviewRequestedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_assignees",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    @Builder.Default
    private Set<Member> assignees = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_approvers",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    @Builder.Default
    private Set<Member> approvers = new LinkedHashSet<>();

    public static Task create(
        Project project,
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        Boolean urgent,
        Integer requiredReviewerCount,
        Set<Tag> tags,
        Set<Member> assignees
    ) {
        return Task.builder()
            .project(project)
            .title(title)
            .description(description)
            .status(status)
            .dueDate(dueDate)
            .urgent(urgent)
            .requiredReviewerCount(requiredReviewerCount)
            .tags(tags)
            .assignees(assignees)
            .build();
    }

    public void update(
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        Boolean urgent,
        Integer requiredReviewerCount,
        Set<Tag> tags,
        Set<Member> assignees
    ) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.urgent = urgent;
        this.requiredReviewerCount = requiredReviewerCount;
        this.tags = tags;
        this.assignees = assignees;
    }

    public void changeStatus(TaskStatus taskStatus) {
        this.status = taskStatus;
    }

    public void approve(Member member) {
        if (assignees.contains(member)) {
            throw new BusinessException(
                ErrorCode.INVALID_APPROVER, "memberId: " + member.getId()
            );
        }

        if (approvers.contains(member)) {
            throw new BusinessException(
                ErrorCode.ALREADY_APPROVED, "memberId: " + member.getId()
            );
        }

        approvers.add(member);
    }

    public void requestReReview(LocalDateTime now) {
        this.reReviewRequestedAt = now;
    }

    public int getRequiredApprovalsCount(List<Member> projectMembers) {
        Set<Member> availableReviewers = new HashSet<>(projectMembers);
        availableReviewers.removeAll(assignees);
        return Math.min(requiredReviewerCount, availableReviewers.size());
    }

    public void ensureTaskInProject(UUID projectId) {
        if (!this.getProject().getId().equals(projectId)) {
            throw new BusinessException(
                ErrorCode.TASK_NOT_IN_PROJECT,
                "projectId: " + projectId + ", taskId: " + this.getId()
            );
        }
    }
}
