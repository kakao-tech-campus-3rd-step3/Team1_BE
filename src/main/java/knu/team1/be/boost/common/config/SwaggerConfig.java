package knu.team1.be.boost.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // API 문서의 기본 정보
        Info info = new Info()
            .title("Boost API")
            .description("Boost 프로젝트 REST API 문서")
            .version("v1.0.0");

        // JWT 인증 방식
        SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name(HttpHeaders.AUTHORIZATION);

        return new OpenAPI()
            .info(info)
            .components(
                new Components().addSecuritySchemes("bearerAuth", securityScheme)
            )
            .addServersItem(
                new Server().url("https://api.boost.ai.kr").description("Production Server")
            )
            .addServersItem(
                new Server().url("http://localhost:8080").description("Local Server")
            );
    }
}
