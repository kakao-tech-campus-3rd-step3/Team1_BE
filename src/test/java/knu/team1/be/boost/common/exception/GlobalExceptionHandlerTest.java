package knu.team1.be.boost.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import knu.team1.be.boost.security.filter.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)

@ContextConfiguration(classes = {
    GlobalExceptionHandler.class,
    GlobalExceptionHandlerTest.TestController.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Bean Validation 오류 시 400 응답과 적절한 에러 메시지를 반환한다")
    void handleValidation() throws Exception {
        TestRequest invalidRequest = new TestRequest("", null);

        mockMvc.perform(post("/test/validation").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("입력값이 올바르지 않습니다."))
            .andExpect(jsonPath("$.instance").value("/test/validation"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[0].field").exists())
            .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    @DisplayName("타입 불일치 시 400 응답과 적절한 에러 메시지를 반환한다")
    void handleTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/type-mismatch").param("id", "invalid-number"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("파라미터 타입이 올바르지 않습니다: id"))
            .andExpect(jsonPath("$.instance").value("/test/type-mismatch"));
    }

    @Test
    @DisplayName("JSON 파싱 불가 시 400 응답과 적절한 에러 메시지를 반환한다")
    void handleNotReadable() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(
                post("/test/json").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("요청 본문을 해석할 수 없습니다."))
            .andExpect(jsonPath("$.instance").value("/test/json"));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드 시 405 응답을 반환한다")
    void handleMethodNotSupported() throws Exception {
        mockMvc.perform(delete("/test/validation"))
            .andDo(print())
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.title").value("Method Not Allowed"))
            .andExpect(jsonPath("$.instance").value("/test/validation"));
    }

    @Test
    @DisplayName("지원하지 않는 미디어 타입 시 415 응답을 반환한다")
    void handleMediaTypeNotSupported() throws Exception {
        mockMvc.perform(
                post("/test/validation").contentType(MediaType.TEXT_PLAIN).content("plain text"))
            .andDo(print())
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(jsonPath("$.status").value(415))
            .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
            .andExpect(jsonPath("$.instance").value("/test/validation"));
    }

    @Test
    @DisplayName("예상치 못한 예외 시 500 응답과 일반적인 에러 메시지를 반환한다")
    void handleFallback() throws Exception {
        mockMvc.perform(get("/test/exception"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.detail").value("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."))
            .andExpect(jsonPath("$.instance").value("/test/exception"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/validation")
        public Map<String, String> testValidation(@Valid @RequestBody TestRequest request) {
            return Map.of("message", "success");
        }

        @GetMapping("/type-mismatch")
        public Map<String, String> testTypeMismatch(@RequestParam Long id) {
            return Map.of("id", id.toString());
        }

        @PostMapping("/json")
        public Map<String, String> testJson(@RequestBody TestRequest request) {
            return Map.of("message", "success");
        }

        @GetMapping("/exception")
        public Map<String, String> testException() {
            throw new RuntimeException("테스트 예외");
        }
    }

    static class TestRequest {

        @NotBlank(message = "이름은 필수입니다")
        private String name;

        @NotNull(message = "나이는 필수입니다")
        private Integer age;

        public TestRequest() {
        }

        public TestRequest(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
