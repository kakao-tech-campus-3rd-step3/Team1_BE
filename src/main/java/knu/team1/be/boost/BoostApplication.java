package knu.team1.be.boost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class BoostApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoostApplication.class, args);
    }
}
