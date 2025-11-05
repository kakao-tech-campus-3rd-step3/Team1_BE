package knu.team1.be.boost.ai.config;

import java.util.concurrent.Executor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AiConfig {

    @Value("${llm.prompt}")
    private String llmPrompt;

    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-task-");
        executor.initialize();
        return executor;
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(llmPrompt)
            .build();
    }
}
