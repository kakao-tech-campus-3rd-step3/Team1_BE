package knu.team1.be.boost.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                // todo: 권한 제한 필요
                .anyRequest().permitAll())
            // todo: production 환경에서 enable 전환 필요
            .csrf(csrf -> csrf.disable()
            );

        return http.build();
    }

}

