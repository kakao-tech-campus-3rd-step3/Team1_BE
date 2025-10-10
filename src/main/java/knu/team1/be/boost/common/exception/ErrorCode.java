package knu.team1.be.boost.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Member 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found", "해당 사용자를 찾을 수 없습니다."),
    MEMBER_ALREADY_JOINED(HttpStatus.CONFLICT, "Member already joined", "이미 참여한 멤버입니다."),

    // Project 관련
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Project not found", "해당 프로젝트를 찾을 수 없습니다."),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Project member not found",
        "해당 프로젝트에 참여하지 않은 멤버입니다."),
    PROJECT_MEMBER_ONLY(HttpStatus.FORBIDDEN, "Project member only", "프로젝트 멤버만 사용할 수 있습니다."),
    PROJECT_OWNER_ONLY(HttpStatus.FORBIDDEN, "Project owner only", "프로젝트 소유자만 수행할 수 있습니다."),

    // Project Join Code 관련
    EXPIRED_JOIN_CODE(HttpStatus.BAD_REQUEST, "Expired join code", "만료된 참가 코드입니다."),
    JOIN_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "Join code not found", "참가 코드를 찾을 수 없습니다."),

    // Task 관련
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "Task not found", "할 일을 찾을 수 없습니다."),
    TASK_NOT_IN_PROJECT(HttpStatus.CONFLICT, "Task not in project", "해당 할 일이 프로젝트에 속하지 않습니다."),
    TASK_ASSIGNEE_ONLY(HttpStatus.FORBIDDEN, "Task assignee only", "해당 할 일의 담당자만 수행할 수 있습니다."),
    INVALID_SORT_OPTION(HttpStatus.BAD_REQUEST, "Invalid sort option", "올바르지 않은 정렬 옵션입니다."),
    INVALID_APPROVER(HttpStatus.FORBIDDEN, "Invalid approver", "담당자는 승인할 수 없습니다."),
    ALREADY_APPROVED(HttpStatus.CONFLICT, "Already approved", "이미 승인한 사용자입니다."),

    // File 관련
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "File not found", "파일을 찾을 수 없습니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "File too large", "파일 크기가 너무 큽니다."),
    FILE_ALREADY_UPLOAD_COMPLETED(HttpStatus.CONFLICT, "File already upload completed",
        "이미 업로드가 완료된 파일입니다."),
    FILE_NOT_READY(HttpStatus.CONFLICT, "File not ready", "아직 다운로드할 수 없는 상태의 파일입니다."),
    STORAGE_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Storage service error",
        "파일 저장 서비스에 오류가 발생했습니다."),

    // Comment 관련
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment not found", "댓글을 찾을 수 없습니다."),
    COMMENT_AUTHOR_ONLY(HttpStatus.FORBIDDEN, "Comment author only", "댓글 작성자만 수행할 수 있습니다."),

    // Auth 관련
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid refresh token", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token not found", "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_EQUALS(HttpStatus.UNAUTHORIZED, "Refresh token not equals",
        "리프레시 토큰이 일치하지 않습니다."),
    KAKAO_INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "Kakao invalid auth code",
        "카카오 인가 코드가 유효하지 않습니다."),

    // JWT 관련
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token expired", "만료된 토큰입니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "Invalid signature", "잘못된 서명이거나 유효하지 않은 형식의 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "Unsupported token", "지원하지 않는 형식의 토큰입니다."),
    MISSING_CLAIMS(HttpStatus.BAD_REQUEST, "Missing claims", "토큰에 필요한 클레임이 누락되었습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed",
        "인증에 실패했습니다. 유효한 토큰이 필요합니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;
    private final String clientMessage;

}
