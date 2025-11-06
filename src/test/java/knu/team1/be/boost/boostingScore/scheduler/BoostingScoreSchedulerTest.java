package knu.team1.be.boost.boostingScore.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.boostingScore.service.BoostingScoreService;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoostingScoreSchedulerTest {

    @InjectMocks
    private BoostingScoreScheduler boostingScoreScheduler;

    @Mock
    private BoostingScoreService boostingScoreService;

    @Mock
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("스케줄러 실행 성공 - 모든 프로젝트 점수 계산")
    void calculateAllBoostingScores_Success() {
        // given
        Project project1 = Project.builder()
            .id(UUID.randomUUID())
            .name("프로젝트1")
            .build();

        Project project2 = Project.builder()
            .id(UUID.randomUUID())
            .name("프로젝트2")
            .build();

        List<Project> projects = List.of(project1, project2);

        when(projectRepository.findAll()).thenReturn(projects);
        doNothing().when(boostingScoreService)
            .calculateAndSaveScoresForProjectFromScheduler(any(UUID.class));

        // when
        boostingScoreScheduler.calculateAllBoostingScores();

        // then
        verify(projectRepository, times(1)).findAll();
        verify(boostingScoreService, times(2))
            .calculateAndSaveScoresForProjectFromScheduler(any(UUID.class));
        verify(boostingScoreService, times(1))
            .calculateAndSaveScoresForProjectFromScheduler(project1.getId());
        verify(boostingScoreService, times(1))
            .calculateAndSaveScoresForProjectFromScheduler(project2.getId());
    }

    @Test
    @DisplayName("스케줄러 실행 성공 - 프로젝트 없음")
    void calculateAllBoostingScores_NoProjects() {
        // given
        when(projectRepository.findAll()).thenReturn(List.of());

        // when
        boostingScoreScheduler.calculateAllBoostingScores();

        // then
        verify(projectRepository, times(1)).findAll();
        verify(boostingScoreService, never())
            .calculateAndSaveScoresForProjectFromScheduler(any(UUID.class));
    }

    @Test
    @DisplayName("스케줄러 실행 - 일부 프로젝트 실패해도 계속 진행")
    void calculateAllBoostingScores_PartialFailure() {
        // given
        Project project1 = Project.builder()
            .id(UUID.randomUUID())
            .name("프로젝트1")
            .build();

        Project project2 = Project.builder()
            .id(UUID.randomUUID())
            .name("프로젝트2")
            .build();

        Project project3 = Project.builder()
            .id(UUID.randomUUID())
            .name("프로젝트3")
            .build();

        List<Project> projects = List.of(project1, project2, project3);

        when(projectRepository.findAll()).thenReturn(projects);
        doNothing().when(boostingScoreService)
            .calculateAndSaveScoresForProjectFromScheduler(project1.getId());
        doThrow(new RuntimeException("점수 계산 실패"))
            .when(boostingScoreService)
            .calculateAndSaveScoresForProjectFromScheduler(project2.getId());
        doNothing().when(boostingScoreService)
            .calculateAndSaveScoresForProjectFromScheduler(project3.getId());

        // when
        boostingScoreScheduler.calculateAllBoostingScores();

        // then
        verify(projectRepository, times(1)).findAll();
        verify(boostingScoreService, times(3))
            .calculateAndSaveScoresForProjectFromScheduler(any(UUID.class));
        verify(boostingScoreService, times(1))
            .calculateAndSaveScoresForProjectFromScheduler(project1.getId());
        verify(boostingScoreService, times(1))
            .calculateAndSaveScoresForProjectFromScheduler(project2.getId());
        verify(boostingScoreService, times(1))
            .calculateAndSaveScoresForProjectFromScheduler(project3.getId());
    }

    @Test
    @DisplayName("스케줄러 실행 - ProjectRepository에서 예외 발생")
    void calculateAllBoostingScores_RepositoryException() {
        // given
        when(projectRepository.findAll())
            .thenThrow(new RuntimeException("DB 연결 오류"));

        // when
        boostingScoreScheduler.calculateAllBoostingScores();

        // then
        verify(projectRepository, times(1)).findAll();
        verify(boostingScoreService, never())
            .calculateAndSaveScoresForProjectFromScheduler(any(UUID.class));
    }

    @Test
    @DisplayName("스케줄러 실행 - 모든 프로젝트 계산 실패")
    void calculateAllBoostingScores_AllFailed() {
        // given
        Project project1 = Project.builder()
            .id(UUID.randomUUID())
            .name("프로젝트1")
            .build();

        Project project2 = Project.builder()
            .id(UUID.randomUUID())
            .name("프로젝트2")
            .build();

        List<Project> projects = List.of(project1, project2);

        when(projectRepository.findAll()).thenReturn(projects);
        doThrow(new RuntimeException("점수 계산 실패1"))
            .when(boostingScoreService)
            .calculateAndSaveScoresForProjectFromScheduler(project1.getId());
        doThrow(new RuntimeException("점수 계산 실패2"))
            .when(boostingScoreService)
            .calculateAndSaveScoresForProjectFromScheduler(project2.getId());

        // when
        boostingScoreScheduler.calculateAllBoostingScores();

        // then
        verify(projectRepository, times(1)).findAll();
        verify(boostingScoreService, times(2))
            .calculateAndSaveScoresForProjectFromScheduler(any(UUID.class));
    }

    @Test
    @DisplayName("스케줄러 실행 성공 - 단일 프로젝트")
    void calculateAllBoostingScores_SingleProject() {
        // given
        Project project = Project.builder()
            .id(UUID.randomUUID())
            .name("단일 프로젝트")
            .build();

        when(projectRepository.findAll()).thenReturn(List.of(project));
        doNothing().when(boostingScoreService)
            .calculateAndSaveScoresForProjectFromScheduler(project.getId());

        // when
        boostingScoreScheduler.calculateAllBoostingScores();

        // then
        verify(projectRepository, times(1)).findAll();
        verify(boostingScoreService, times(1))
            .calculateAndSaveScoresForProjectFromScheduler(project.getId());
    }

    @Test
    @DisplayName("스케줄러 실행 성공 - 다수의 프로젝트")
    void calculateAllBoostingScores_ManyProjects() {
        // given
        List<Project> projects = List.of(
            Project.builder().id(UUID.randomUUID()).name("프로젝트1").build(),
            Project.builder().id(UUID.randomUUID()).name("프로젝트2").build(),
            Project.builder().id(UUID.randomUUID()).name("프로젝트3").build(),
            Project.builder().id(UUID.randomUUID()).name("프로젝트4").build(),
            Project.builder().id(UUID.randomUUID()).name("프로젝트5").build()
        );

        when(projectRepository.findAll()).thenReturn(projects);
        doNothing().when(boostingScoreService)
            .calculateAndSaveScoresForProjectFromScheduler(any(UUID.class));

        // when
        boostingScoreScheduler.calculateAllBoostingScores();

        // then
        verify(projectRepository, times(1)).findAll();
        verify(boostingScoreService, times(5))
            .calculateAndSaveScoresForProjectFromScheduler(any(UUID.class));
    }
}

