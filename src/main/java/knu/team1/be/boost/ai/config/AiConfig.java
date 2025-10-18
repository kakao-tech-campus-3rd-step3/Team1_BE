package knu.team1.be.boost.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(
                "이 글을 좀 더 부드러운 말투로 변형해줘.")
            .build();
    }
}
