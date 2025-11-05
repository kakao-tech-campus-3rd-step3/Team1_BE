package knu.team1.be.boost.webPush.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import knu.team1.be.boost.security.filter.JwtAuthFilter;
import knu.team1.be.boost.webPush.dto.WebPushConnectDto;
import knu.team1.be.boost.webPush.dto.WebPushRegisterDto;
import knu.team1.be.boost.webPush.dto.WebPushSessionResponseDto;
import knu.team1.be.boost.webPush.dto.WebPushSessionStatus;
import knu.team1.be.boost.webPush.service.WebPushService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = WebPushController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class WebPushControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebPushService webPushService;

    private static final String TOKEN = "2f8f2a6e-0c48-4e0f-9f5f-123456789abc";

    @Nested
    @DisplayName("웹푸시 세션 생성")
    class CreateSession {

        @Test
        @DisplayName("세션 생성 성공 - 200 OK")
        void success_createSession() throws Exception {
            // given
            WebPushSessionResponseDto response =
                new WebPushSessionResponseDto(TOKEN, WebPushSessionStatus.CREATED);

            given(webPushService.createSession(any())).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/web-push/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.status").value("CREATED"));
        }
    }

    @Nested
    @DisplayName("디바이스 연결")
    class ConnectDevice {

        @Test
        @DisplayName("디바이스 연결 성공 - 200 OK")
        void success_connectDevice() throws Exception {
            // given
            WebPushConnectDto request = new WebPushConnectDto(TOKEN, "Chrome 128.0");
            WebPushSessionResponseDto response =
                new WebPushSessionResponseDto(TOKEN, WebPushSessionStatus.CONNECTED);

            given(webPushService.connectDevice(any(WebPushConnectDto.class))).willReturn(response);

            // when & then
            mockMvc.perform(
                    post("/api/web-push/sessions/connect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.status").value("CONNECTED"));
        }

        @Test
        @DisplayName("디바이스 연결 실패 - 400 (빈 token)")
        void fail_blankToken() throws Exception {
            // given
            WebPushConnectDto request = new WebPushConnectDto("", "Chrome");

            // when & then
            mockMvc.perform(
                    post("/api/web-push/sessions/connect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("디바이스 연결 실패 - 400 (빈 deviceInfo)")
        void fail_blankDeviceInfo() throws Exception {
            // given
            WebPushConnectDto request = new WebPushConnectDto(TOKEN, "");

            // when & then
            mockMvc.perform(
                    post("/api/web-push/sessions/connect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("세션 상태 조회")
    class GetSessionStatus {

        @Test
        @DisplayName("세션 상태 조회 성공 - 200 OK")
        void success_getSessionStatus() throws Exception {
            // given
            WebPushSessionResponseDto response =
                new WebPushSessionResponseDto(TOKEN, WebPushSessionStatus.CONNECTED);

            given(webPushService.getSessionStatus(eq(TOKEN))).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/web-push/sessions/{token}", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.status").value("CONNECTED"));
        }
    }

    @Nested
    @DisplayName("웹푸시 구독 등록")
    class RegisterSubscription {

        @Test
        @DisplayName("구독 등록 성공 - 200 OK")
        void success_registerSubscription() throws Exception {
            // given
            WebPushRegisterDto request = new WebPushRegisterDto(
                TOKEN,
                "https://example.com/endpoint",
                "p256dh-key",
                "auth-key"
            );

            WebPushSessionResponseDto response =
                new WebPushSessionResponseDto(TOKEN, WebPushSessionStatus.REGISTERED);

            given(webPushService.registerSubscription(any(WebPushRegisterDto.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                    post("/api/web-push/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.status").value("REGISTERED"));
        }

        @Test
        @DisplayName("구독 등록 실패 - 400 (빈 필드)")
        void fail_blankFields() throws Exception {
            // given
            WebPushRegisterDto request = new WebPushRegisterDto("", "", "", "");

            // when & then
            mockMvc.perform(
                    post("/api/web-push/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }
    }
}
