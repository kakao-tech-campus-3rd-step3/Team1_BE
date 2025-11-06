package knu.team1.be.boost.boostingScore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoostingScoreServiceTest {

    @InjectMocks
    private BoostingScoreService boostingScoreService;

    @Mock
    private BoostingScoreRepository boostingScoreRepository;

    @Mock
    private ProjectMembershipRepository projectMembershipRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BoostingScoreConfig scoreConfig;

    @Mock
    private BoostingScoreConfig.TaskScoreConfig taskScoreConfig;

    @Mock
    private AccessPolicy accessPolicy;

    private final UUID projectId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    private Project testProject;
    private Member testMember;
    private ProjectMembership testMembership;
    private BoostingScore testScore;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
            .id(projectId)
            .name("테스트 프로젝트")
            .build();

        testMember = Member.builder()
            .id(memberId)
            .name("테스트유저")
            .avatar("1111")
            .build();

        testMembership = ProjectMembership.builder()
            .id(UUID.randomUUID())
            .project(testProject)
            .member(testMember)
            .role(ProjectRole.MEMBER)
            .build();

        testScore = BoostingScore.create(
            testMembership,
            10,
            5,
            6,
            LocalDateTime.now()
        );
    }

    private void setupScoreConfig() {
        lenient().when(scoreConfig.getTask()).thenReturn(taskScoreConfig);
        lenient().when(scoreConfig.getCommentScore()).thenReturn(1);
        lenient().when(scoreConfig.getApproveScore()).thenReturn(3);
        lenient().when(taskScoreConfig.getTodo()).thenReturn(1);
        lenient().when(taskScoreConfig.getProgress()).thenReturn(2);
        lenient().when(taskScoreConfig.getReview()).thenReturn(3);
        lenient().when(taskScoreConfig.getDone()).thenReturn(5);
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 성공 - 기존 점수 존재")
    void getProjectBoostingScores_Success_ExistingScores() {
        // given
        List<BoostingScore> scores = List.of(testScore);

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(boostingScoreRepository.existsByProjectId(projectId)).thenReturn(true);
        when(boostingScoreRepository.findLatestByProjectId(projectId)).thenReturn(scores);

        // when
        List<BoostingScoreResponseDto> result = boostingScoreService.getProjectBoostingScores(
            projectId,
            memberId
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).memberId()).isEqualTo(memberId);
        assertThat(result.get(0).totalScore()).isEqualTo(21);
        assertThat(result.get(0).rank()).isEqualTo(1);

        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(boostingScoreRepository, times(1)).existsByProjectId(projectId);
        verify(boostingScoreRepository, times(1)).findLatestByProjectId(projectId);
        verify(boostingScoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 성공 - 점수 없음, 새로 계산")
    void getProjectBoostingScores_Success_CalculateNew() {
        // given
        setupScoreConfig();
        List<BoostingScore> scores = List.of(testScore);
        List<Task> tasks = List.of(
            Task.builder().id(UUID.randomUUID()).status(TaskStatus.DONE).build()
        );

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(boostingScoreRepository.existsByProjectId(projectId)).thenReturn(false);
        when(projectMembershipRepository.findAllByProjectId(projectId))
            .thenReturn(List.of(testMembership));
        when(taskRepository.findAllByProjectIdAndAssigneesId(projectId, memberId))
            .thenReturn(tasks);
        when(commentRepository.countByTaskProjectIdAndMemberId(projectId, memberId))
            .thenReturn(5L);
        when(taskRepository.countByProjectIdAndApproversId(projectId, memberId))
            .thenReturn(2L);
        when(boostingScoreRepository.save(any(BoostingScore.class)))
            .thenReturn(testScore);
        when(boostingScoreRepository.findLatestByProjectId(projectId)).thenReturn(scores);

        // when
        List<BoostingScoreResponseDto> result = boostingScoreService.getProjectBoostingScores(
            projectId,
            memberId
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);

        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(boostingScoreRepository, times(1)).existsByProjectId(projectId);
        verify(projectMembershipRepository, times(1)).findAllByProjectId(projectId);
        verify(boostingScoreRepository, times(1)).save(any(BoostingScore.class));
        verify(boostingScoreRepository, times(1)).findLatestByProjectId(projectId);
    }

    @Test
    @DisplayName("프로젝트 공헌도 점수 조회 실패 - 권한 없음")
    void getProjectBoostingScores_Fail_NoPermission() {
        // given
        doThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY))
            .when(accessPolicy).ensureProjectMember(projectId, memberId);

        // when & then
        assertThatThrownBy(
            () -> boostingScoreService.getProjectBoostingScores(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);

        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(boostingScoreRepository, never()).existsByProjectId(any());
    }

    @Test
    @DisplayName("API 호출로 점수 계산 및 저장 성공")
    void calculateAndSaveScoresForProjectFromApi_Success() {
        // given
        setupScoreConfig();
        List<Task> tasks = List.of(
            Task.builder().id(UUID.randomUUID()).status(TaskStatus.DONE).build(),
            Task.builder().id(UUID.randomUUID()).status(TaskStatus.REVIEW).build()
        );

        when(projectMembershipRepository.findAllByProjectId(projectId))
            .thenReturn(List.of(testMembership));
        when(taskRepository.findAllByProjectIdAndAssigneesId(projectId, memberId))
            .thenReturn(tasks);
        when(commentRepository.countByTaskProjectIdAndMemberId(projectId, memberId))
            .thenReturn(3L);
        when(taskRepository.countByProjectIdAndApproversId(projectId, memberId))
            .thenReturn(2L);
        when(boostingScoreRepository.save(any(BoostingScore.class)))
            .thenReturn(testScore);

        // when
        boostingScoreService.calculateAndSaveScoresForProjectFromApi(projectId);

        // then
        verify(projectMembershipRepository, times(1)).findAllByProjectId(projectId);
        verify(taskRepository, times(1)).findAllByProjectIdAndAssigneesId(projectId, memberId);
        verify(commentRepository, times(1)).countByTaskProjectIdAndMemberId(projectId, memberId);
        verify(taskRepository, times(1)).countByProjectIdAndApproversId(projectId, memberId);
        verify(boostingScoreRepository, times(1)).save(any(BoostingScore.class));
    }

    @Test
    @DisplayName("API 호출로 점수 계산 - 멤버십 없음")
    void calculateAndSaveScoresForProjectFromApi_NoMemberships() {
        // given
        when(projectMembershipRepository.findAllByProjectId(projectId))
            .thenReturn(List.of());

        // when
        boostingScoreService.calculateAndSaveScoresForProjectFromApi(projectId);

        // then
        verify(projectMembershipRepository, times(1)).findAllByProjectId(projectId);
        verify(boostingScoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("API 호출로 점수 계산 실패 - 모든 멤버 계산 실패")
    void calculateAndSaveScoresForProjectFromApi_Fail_AllMembersFailed() {
        // given
        when(projectMembershipRepository.findAllByProjectId(projectId))
            .thenReturn(List.of(testMembership));
        when(taskRepository.findAllByProjectIdAndAssigneesId(any(), any()))
            .thenThrow(new RuntimeException("DB 오류"));

        // when & then
        assertThatThrownBy(
            () -> boostingScoreService.calculateAndSaveScoresForProjectFromApi(projectId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode",
                ErrorCode.BOOSTING_SCORE_CALCULATION_FAILED);

        verify(projectMembershipRepository, times(1)).findAllByProjectId(projectId);
        verify(boostingScoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("스케줄러 호출로 점수 계산 및 저장 성공")
    void calculateAndSaveScoresForProjectFromScheduler_Success() {
        // given
        setupScoreConfig();
        List<Task> tasks = List.of(
            Task.builder().id(UUID.randomUUID()).status(TaskStatus.TODO).build()
        );

        when(projectMembershipRepository.findAllByProjectId(projectId))
            .thenReturn(List.of(testMembership));
        when(taskRepository.findAllByProjectIdAndAssigneesId(projectId, memberId))
            .thenReturn(tasks);
        when(commentRepository.countByTaskProjectIdAndMemberId(projectId, memberId))
            .thenReturn(10L);
        when(taskRepository.countByProjectIdAndApproversId(projectId, memberId))
            .thenReturn(5L);
        when(boostingScoreRepository.save(any(BoostingScore.class)))
            .thenReturn(testScore);

        // when
        boostingScoreService.calculateAndSaveScoresForProjectFromScheduler(projectId);

        // then
        verify(projectMembershipRepository, times(1)).findAllByProjectId(projectId);
        verify(boostingScoreRepository, times(1)).save(any(BoostingScore.class));
    }

    @Test
    @DisplayName("스케줄러 호출로 점수 계산 - 일부 멤버 실패해도 계속 진행")
    void calculateAndSaveScoresForProjectFromScheduler_PartialFailure() {
        // given
        setupScoreConfig();
        Member member2 = Member.builder()
            .id(UUID.randomUUID())
            .name("멤버2")
            .avatar("2222")
            .build();

        ProjectMembership membership2 = ProjectMembership.builder()
            .id(UUID.randomUUID())
            .project(testProject)
            .member(member2)
            .role(ProjectRole.MEMBER)
            .build();

        when(projectMembershipRepository.findAllByProjectId(projectId))
            .thenReturn(List.of(testMembership, membership2));
        when(taskRepository.findAllByProjectIdAndAssigneesId(projectId, memberId))
            .thenThrow(new RuntimeException("첫 번째 멤버 오류"));
        when(taskRepository.findAllByProjectIdAndAssigneesId(projectId, member2.getId()))
            .thenReturn(List.of());
        when(commentRepository.countByTaskProjectIdAndMemberId(projectId, member2.getId()))
            .thenReturn(1L);
        when(taskRepository.countByProjectIdAndApproversId(projectId, member2.getId()))
            .thenReturn(0L);
        when(boostingScoreRepository.save(any(BoostingScore.class)))
            .thenReturn(testScore);

        // when
        boostingScoreService.calculateAndSaveScoresForProjectFromScheduler(projectId);

        // then
        verify(projectMembershipRepository, times(1)).findAllByProjectId(projectId);
        verify(boostingScoreRepository, times(1)).save(any(BoostingScore.class));
    }

    @Test
    @DisplayName("Task 점수 계산 - 다양한 상태")
    void calculateTaskScore_VariousStatuses() {
        // given
        setupScoreConfig();
        List<Task> tasks = List.of(
            Task.builder().id(UUID.randomUUID()).status(TaskStatus.TODO).build(),
            Task.builder().id(UUID.randomUUID()).status(TaskStatus.PROGRESS).build(),
            Task.builder().id(UUID.randomUUID()).status(TaskStatus.REVIEW).build(),
            Task.builder().id(UUID.randomUUID()).status(TaskStatus.DONE).build()
        );

        when(projectMembershipRepository.findAllByProjectId(projectId))
            .thenReturn(List.of(testMembership));
        when(taskRepository.findAllByProjectIdAndAssigneesId(projectId, memberId))
            .thenReturn(tasks);
        when(commentRepository.countByTaskProjectIdAndMemberId(projectId, memberId))
            .thenReturn(0L);
        when(taskRepository.countByProjectIdAndApproversId(projectId, memberId))
            .thenReturn(0L);
        when(boostingScoreRepository.save(any(BoostingScore.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        boostingScoreService.calculateAndSaveScoresForProjectFromApi(projectId);

        // then
        verify(boostingScoreRepository, times(1)).save(any(BoostingScore.class));
    }
}

