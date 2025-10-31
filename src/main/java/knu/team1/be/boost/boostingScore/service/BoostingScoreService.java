package knu.team1.be.boost.boostingScore.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.boostingScore.config.BoostingScoreConfig;
import knu.team1.be.boost.boostingScore.dto.BoostingScoreResponseDto;
import knu.team1.be.boost.boostingScore.entity.BoostingScore;
import knu.team1.be.boost.boostingScore.repository.BoostingScoreRepository;
import knu.team1.be.boost.comment.repository.CommentRepository;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoostingScoreService {

    private final BoostingScoreRepository boostingScoreRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final BoostingScoreConfig scoreConfig;
    private final AccessPolicy accessPolicy;

    public List<BoostingScoreResponseDto> getProjectBoostingScores(
        UUID projectId,
        UUID memberId
    ) {
        accessPolicy.ensureProjectMember(projectId, memberId);

        if (!boostingScoreRepository.existsByProjectId(projectId)) {
            calculateAndSaveScoresForProjectFromApi(projectId);
        }

        List<BoostingScore> scores = boostingScoreRepository.findLatestByProjectId(projectId);

        return scores.stream()
            .map(score -> BoostingScoreResponseDto.from(
                score,
                scores.indexOf(score) + 1
            ))
            .toList();
    }

    @Transactional
    public void calculateAndSaveScoresForProjectFromApi(UUID projectId) {
        LocalDateTime calculatedAt = LocalDateTime.now();

        List<ProjectMembership> memberships =
            projectMembershipRepository.findAllByProjectId(projectId);

        if (memberships.isEmpty()) {
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (ProjectMembership membership : memberships) {
            try {
                BoostingScore score = calculateScoreForMember(
                    membership,
                    calculatedAt
                );
                boostingScoreRepository.save(score);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error(
                    "Failed to calculate score for member: {}, project: {}",
                    membership.getMember().getId(),
                    projectId,
                    e
                );
            }
        }

        if (successCount == 0 && failureCount > 0) {
            throw new BusinessException(
                ErrorCode.BOOSTING_SCORE_CALCULATION_FAILED,
                "projectId: " + projectId + ", all members failed"
            );
        }
    }

    @Transactional
    public void calculateAndSaveScoresForProjectFromScheduler(UUID projectId) {
        LocalDateTime calculatedAt = LocalDateTime.now();

        List<ProjectMembership> memberships =
            projectMembershipRepository.findAllByProjectId(projectId);

        for (ProjectMembership membership : memberships) {
            try {
                BoostingScore score = calculateScoreForMember(
                    membership,
                    calculatedAt
                );
                boostingScoreRepository.save(score);
            } catch (Exception e) {
                log.error(
                    "Failed to calculate score for member: {}, project: {}",
                    membership.getMember().getId(),
                    projectId,
                    e
                );
            }
        }
    }

    /**
     * 특정 멤버의 Boosting Score를 계산합니다.
     */
    private BoostingScore calculateScoreForMember(
        ProjectMembership membership,
        LocalDateTime calculatedAt
    ) {
        Member member = membership.getMember();
        Project project = membership.getProject();

        Integer taskScore = calculateTaskScore(project.getId(), member.getId());

        Integer commentScore = calculateCommentScore(project.getId(), member.getId());

        Integer approveScore = calculateApproveScore(project.getId(), member.getId());

        return BoostingScore.create(
            membership,
            taskScore,
            commentScore,
            approveScore,
            calculatedAt
        );
    }

    private Integer calculateTaskScore(UUID projectId, UUID memberId) {
        List<Task> tasks = taskRepository.findAllByProjectIdAndAssigneesId(projectId, memberId);

        int totalScore = 0;
        for (Task task : tasks) {
            totalScore += getTaskScoreByStatus(task.getStatus());
        }

        return totalScore;
    }

    private Integer getTaskScoreByStatus(TaskStatus status) {
        return switch (status) {
            case TODO -> scoreConfig.getTask().getTodo();
            case PROGRESS -> scoreConfig.getTask().getProgress();
            case REVIEW -> scoreConfig.getTask().getReview();
            case DONE -> scoreConfig.getTask().getDone();
        };
    }

    private Integer calculateCommentScore(UUID projectId, UUID memberId) {
        Long commentCount = commentRepository.countByTaskProjectIdAndMemberId(
            projectId,
            memberId
        );

        return commentCount.intValue() * scoreConfig.getCommentScore();
    }

    private Integer calculateApproveScore(UUID projectId, UUID memberId) {
        Long approveCount = taskRepository.countByProjectIdAndApproversId(
            projectId,
            memberId
        );

        return approveCount.intValue() * scoreConfig.getApproveScore();
    }
}

