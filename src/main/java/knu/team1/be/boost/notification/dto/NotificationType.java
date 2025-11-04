package knu.team1.be.boost.notification.dto;

public enum NotificationType {

    REVIEW("작업 검토 요청", "[%s] 작업이 검토 중 상태로 변경되었습니다."),
    RE_REVIEW("작업 재검토 요청", "[%s] 작업의 재검토가 요청되었습니다."),
    APPROVED("작업 승인 완료", "[%s] 작업이 모든 승인자를 통해 승인되었습니다.");

    private final String title;
    private final String messageFormat;

    NotificationType(String title, String messageFormat) {
        this.title = title;
        this.messageFormat = messageFormat;
    }

    public String title() {
        return title;
    }

    public String message(String taskTitle) {
        return String.format(messageFormat, taskTitle);
    }
}
