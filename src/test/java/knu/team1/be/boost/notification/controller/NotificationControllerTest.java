package knu.team1.be.boost.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.notification.dto.NotificationCountResponseDto;
import knu.team1.be.boost.notification.dto.NotificationListResponseDto;
import knu.team1.be.boost.notification.dto.NotificationReadResponseDto;
import knu.team1.be.boost.notification.dto.NotificationResponseDto;
import knu.team1.be.boost.notification.dto.ProjectNotificationResponseDto;
import knu.team1.be.boost.notification.service.NotificationService;
import knu.team1.be.boost.security.filter.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = NotificationController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Nested
    @DisplayName("알림 목록 조회")
    class GetNotifications {

        @Test
        @DisplayName("알림 목록 조회 성공 - 200 OK + 커서 정보 포함")
        void getNotifications_success_withCursor() throws Exception {
            UUID nextCursor = Fixtures.id();
            List<NotificationResponseDto> notifications = List.of(
                Fixtures.notification(Fixtures.id(), "작업 승인 완료", "작업이 승인되었습니다.", false),
                Fixtures.notification(Fixtures.id(), "댓글 등록", "새 댓글이 등록되었습니다.", true)
            );

            NotificationListResponseDto response =
                new NotificationListResponseDto(notifications, 2, nextCursor, true);

            given(notificationService.getNotifications(any(), eq(20), any()))
                .willReturn(response);

            mockMvc.perform(get("/api/notifications").param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.notifications[0].title").value("작업 승인 완료"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(nextCursor.toString()));
        }

        @Test
        @DisplayName("알림 목록 조회 실패 - 400 (limit > 50)")
        void getNotifications_fail_invalidLimit() throws Exception {
            mockMvc.perform(get("/api/notifications").param("limit", "100"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("알림 목록 조회 실패 - 404 (존재하지 않는 사용자)")
        void getNotifications_fail_memberNotFound() throws Exception {
            given(notificationService.getNotifications(any(), anyInt(), any()))
                .willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("알림 목록 조회 실패 - 500 (서버 내부 오류)")
        void getNotifications_fail_serverError() throws Exception {
            given(notificationService.getNotifications(any(), anyInt(), any()))
                .willThrow(new RuntimeException("Unexpected"));

            mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("알림 읽음 처리 성공 - 200 OK")
        void markAsRead_success() throws Exception {
            UUID notificationId = Fixtures.id();
            NotificationReadResponseDto response =
                new NotificationReadResponseDto(notificationId, true);

            given(notificationService.markAsRead(eq(notificationId), any()))
                .willReturn(response);

            mockMvc.perform(patch("/api/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notificationId.toString()))
                .andExpect(jsonPath("$.read").value(true));
        }

        @Test
        @DisplayName("알림 읽음 처리 실패 - 403 (다른 사용자의 알림 접근)")
        void markAsRead_fail_forbidden() throws Exception {
            UUID notificationId = Fixtures.id();
            given(notificationService.markAsRead(eq(notificationId), any()))
                .willThrow(new BusinessException(ErrorCode.NOTIFICATION_FORBIDDEN_ACCESS));

            mockMvc.perform(patch("/api/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("알림 읽음 처리 실패 - 404 (알림 없음)")
        void markAsRead_fail_notFound() throws Exception {
            UUID notificationId = Fixtures.id();
            given(notificationService.markAsRead(eq(notificationId), any()))
                .willThrow(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

            mockMvc.perform(patch("/api/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("모든 알림 읽음 처리")
    class MarkAllAsRead {

        @Test
        @DisplayName("모든 알림 읽음 처리 성공 - 204 No Content")
        void markAllAsRead_success() throws Exception {

            mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("모든 알림 읽음 처리 실패 - 404 (존재하지 않는 사용자)")
        void markAllAsRead_fail_memberNotFound() throws Exception {
            doThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
                .when(notificationService).markAllAsRead(any());

            mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("모든 알림 읽음 처리 실패 - 500 (서버 내부 오류)")
        void markAllAsRead_fail_serverError() throws Exception {
            doThrow(new RuntimeException("Unexpected"))
                .when(notificationService).markAllAsRead(any());

            mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("프로젝트별 알림 설정 변경")
    class SetProjectNotification {

        @Test
        @DisplayName("프로젝트별 알림 설정 변경 성공 - 200 OK")
        void setProjectNotification_success() throws Exception {
            UUID projectId = Fixtures.id();
            UUID memberId = Fixtures.id();

            ProjectNotificationResponseDto response =
                new ProjectNotificationResponseDto(projectId, memberId, true);

            given(notificationService.setProjectNotification(eq(projectId), eq(true), any()))
                .willReturn(response);

            mockMvc.perform(
                    patch("/api/projects/{projectId}/notifications", projectId)
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.enabled").value(true));
        }

        @Test
        @DisplayName("프로젝트별 알림 설정 변경 실패 - 404 (프로젝트 없음)")
        void setProjectNotification_fail_notFound() throws Exception {
            UUID projectId = Fixtures.id();
            given(notificationService.setProjectNotification(eq(projectId), eq(true), any()))
                .willThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

            mockMvc.perform(
                    patch("/api/projects/{projectId}/notifications", projectId)
                        .param("enabled", "true"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("내 알림 개수 조회")
    class GetNotificationCount {

        @Test
        @DisplayName("내 알림 개수 조회 성공 - 200 OK")
        void getNotificationCount_success() throws Exception {
            NotificationCountResponseDto response = new NotificationCountResponseDto(42, 5);
            given(notificationService.getNotificationCount(any())).willReturn(response);

            mockMvc.perform(get("/api/notifications/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(42))
                .andExpect(jsonPath("$.unreadCount").value(5));
        }

        @Test
        @DisplayName("내 알림 개수 조회 실패 - 404 (사용자 없음)")
        void getNotificationCount_fail_notFound() throws Exception {
            given(notificationService.getNotificationCount(any()))
                .willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            mockMvc.perform(get("/api/notifications/count"))
                .andExpect(status().isNotFound());
        }
    }

    static class Fixtures {

        static UUID id() {
            return UUID.randomUUID();
        }

        static NotificationResponseDto notification(
            UUID id, String title, String message, boolean read
        ) {
            return new NotificationResponseDto(
                id, title, message, read, LocalDateTime.now()
            );
        }
    }
}
