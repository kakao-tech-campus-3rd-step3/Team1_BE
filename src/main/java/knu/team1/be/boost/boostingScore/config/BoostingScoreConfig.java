package knu.team1.be.boost.boostingScore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "boosting-score")
public class BoostingScoreConfig {

    private TaskScoreConfig task = new TaskScoreConfig();
    private Integer commentScore = 1;
    private Integer approveScore = 3;

    @Getter
    @Setter
    public static class TaskScoreConfig {
        private Integer todo = 1;
        private Integer progress = 2;
        private Integer review = 3;
        private Integer done = 5;
    }
}

