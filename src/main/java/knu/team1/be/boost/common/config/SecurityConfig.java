package knu.team1.be.boost.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**",
                    "/v3/api-docs/**").permitAll()
                // todo: 권한 제한 필요
                .anyRequest().permitAll())
            // todo: production 환경에서 enable 전환 필요
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                // swagger를 위해 disable
                .frameOptions(frame -> frame.disable())
            );

        return http.build();
    }

    @Configuration
    public static class WebConfig implements WebMvcConfigurer {

        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**").allowedOriginPatterns("*").allowedMethods("*")
                .allowedHeaders("*").allowCredentials(true);
        }
    }

}
