package knu.team1.be.boost.common.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import knu.team1.be.boost.auth.exception.InvalidRefreshTokenException;
import knu.team1.be.boost.auth.exception.KakaoInvalidAuthCodeException;
import knu.team1.be.boost.auth.exception.RefreshTokenNotEqualsException;
import knu.team1.be.boost.auth.exception.RefreshTokenNotFoundException;
import knu.team1.be.boost.file.exception.FileAlreadyUploadCompletedException;
import knu.team1.be.boost.file.exception.FileNotFoundException;
import knu.team1.be.boost.file.exception.FileNotReadyException;
import knu.team1.be.boost.file.exception.FileTooLargeException;
import knu.team1.be.boost.file.exception.StorageServiceException;
import knu.team1.be.boost.member.exception.MemberNotFoundException;
import knu.team1.be.boost.project.exception.ProjectNotFoundException;
import knu.team1.be.boost.task.exception.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private URI instance(HttpServletRequest req) {
        return URI.create(req.getRequestURI());
    }

    // 같은 유형의 예외는 일괄 처리

    // 400: Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
        MethodArgumentNotValidException e,
        HttpServletRequest req
    ) {
        List<Map<String, String>> errors = e.getBindingResult()
            .getFieldErrors().stream()
            .map(fe -> Map.of(
                "field", fe.getField(),
                "message", Optional.ofNullable(
                    fe.getDefaultMessage()).orElse("유효하지 않은 값입니다.")
            ))
            .toList();

        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            "입력값이 올바르지 않습니다.",
            URI.create(req.getRequestURI()),
            Map.of("errors", errors)
        );
    }

    // 400: 잘못된 요청
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            e.getMessage(),
            URI.create(req.getRequestURI())
        );
    }

    // 400: 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e,
        HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            "파라미터 타입이 올바르지 않습니다: " + e.getName(),
            instance(req)
        );
    }

    // 400: JSON 파싱 불가
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            "요청 본문을 해석할 수 없습니다.",
            instance(req)
        );
    }

    // 400: 필수 요청 파라미터가 누락된 경우
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(
        MissingServletRequestParameterException e,
        HttpServletRequest req
    ) {
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());

        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            message,
            instance(req)
        );
    }

    // 400: 인가 코드로 카카오 토큰을 받아오지 못한 경우
    @ExceptionHandler(KakaoInvalidAuthCodeException.class)
    public ProblemDetail handleInvalidAuthCode(KakaoInvalidAuthCodeException e,
        HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            e.getMessage(),
            instance(req)
        );
    }

    // 401: 컨트롤러/서비스 단에서 발생하는 JWT 관련 예외 처리
    // (주로 토큰 재발급 시 만료된 토큰을 파싱하려 할 때 발생)
    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleJwtExceptionInController(JwtException e, HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않은 형식의 토큰입니다.",
            instance(req)
        );
    }

    // 401: 리프레시 토큰이 유효하지 않은 경우
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException e,
        HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.UNAUTHORIZED,
            e.getMessage(),
            instance(req)
        );
    }

    // 401: 요청된 리프레시 토큰과 서버의 리프레시 토큰이 다른 경우
    @ExceptionHandler(RefreshTokenNotEqualsException.class)
    public ProblemDetail handleRefreshTokenNotEquals(RefreshTokenNotEqualsException e,
        HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.UNAUTHORIZED,
            e.getMessage(),
            instance(req)
        );
    }

    // 404: 도메인 NotFound
    @ExceptionHandler({
        FileNotFoundException.class,
        TaskNotFoundException.class,
        MemberNotFoundException.class,
        ProjectNotFoundException.class,
        RefreshTokenNotFoundException.class
    })
    public ProblemDetail handleNotFound(RuntimeException e, HttpServletRequest req) {
        return ErrorResponses.of(HttpStatus.NOT_FOUND, e.getMessage(), instance(req));
    }

    // 409: 리소스 상태 충돌
    @ExceptionHandler({
        FileAlreadyUploadCompletedException.class,
        FileNotReadyException.class
    })
    public ProblemDetail handleAlreadyCompleted(RuntimeException e, HttpServletRequest req) {
        return ErrorResponses.of(HttpStatus.CONFLICT, e.getMessage(), instance(req));
    }

    // 413: 요청 본문이 서버가 허용하는 한도 초과한 경우
    @ExceptionHandler(FileTooLargeException.class)
    public ProblemDetail handleFileTooLarge(FileTooLargeException e, HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.PAYLOAD_TOO_LARGE,
            e.getMessage(),
            instance(req)
        );
    }

    // 405/415 등: 스펙 위반
    @ExceptionHandler({
        HttpRequestMethodNotSupportedException.class,
        HttpMediaTypeNotSupportedException.class
    })
    public ProblemDetail handleMethodMedia(Exception e, HttpServletRequest req) {
        HttpStatus status = (e instanceof HttpRequestMethodNotSupportedException)
            ? HttpStatus.METHOD_NOT_ALLOWED
            : HttpStatus.UNSUPPORTED_MEDIA_TYPE;

        return ErrorResponses.of(status, e.getMessage(), instance(req));
    }

    // 500: 그외 외부 서버와의 오류
    @ExceptionHandler(StorageServiceException.class)
    public ProblemDetail handleStorage(StorageServiceException e, HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            e.getMessage(),
            instance(req)
        );
    }

    // 500: 그외 모든 예외
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleFallback(Exception e, HttpServletRequest req) {
        return ErrorResponses.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            instance(req)
        );
    }
}
