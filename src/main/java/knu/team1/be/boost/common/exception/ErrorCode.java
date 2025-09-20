package knu.team1.be.boost.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Member 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    MEMBER_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여한 멤버입니다."),

    // Project 관련
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다."),

    // Task 관련
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "할 일을 찾을 수 없습니다."),
    TASK_NOT_IN_PROJECT(HttpStatus.CONFLICT, "해당 할 일이 프로젝트에 속하지 않습니다."),

    // File 관련
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기가 너무 큽니다."),
    FILE_ALREADY_UPLOAD_COMPLETED(HttpStatus.CONFLICT, "이미 업로드가 완료된 파일입니다."),
    FILE_NOT_READY(HttpStatus.CONFLICT, "아직 다운로드할 수 없는 상태의 파일입니다."),
    STORAGE_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 서비스에 오류가 발생했습니다."),

    // Auth 관련
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_EQUALS(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다."),
    KAKAO_INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "카카오 인가 코드가 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

}
