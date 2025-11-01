package knu.team1.be.boost.boostingScore.scheduler;

import java.util.List;
import knu.team1.be.boost.boostingScore.service.BoostingScoreService;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostingScoreScheduler {

    private final BoostingScoreService boostingScoreService;
    private final ProjectRepository projectRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void calculateAllBoostingScores() {
        log.info("Starting scheduled boosting score calculation for all projects");

        try {
            List<Project> projects = projectRepository.findAll();

            log.info("Found {} projects to calculate boosting scores", projects.size());

            int successCount = 0;
            int failureCount = 0;

            for (Project project : projects) {
                try {
                    boostingScoreService.calculateAndSaveScoresForProjectFromScheduler(
                        project.getId()
                    );
                    successCount++;
                } catch (Exception e) {
                    log.error(
                        "Failed to calculate boosting scores for project: {}",
                        project.getId(),
                        e
                    );
                    failureCount++;
                }
            }

            log.info(
                "Completed scheduled boosting score calculation. Success: {}, Failure: {}",
                successCount,
                failureCount
            );
        } catch (Exception e) {
            log.error("Fatal error during scheduled boosting score calculation", e);
        }
    }
}

