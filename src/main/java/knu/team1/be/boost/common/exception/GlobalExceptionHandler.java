package knu.team1.be.boost.common.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private URI instance(HttpServletRequest req) {
        return URI.create(req.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException e, HttpServletRequest req) {

        ErrorCode errorCode = e.getErrorCode();
        String errorMessage = errorCode.getErrorMessage();
        HttpStatus httpStatus = errorCode.getHttpStatus();

        if (httpStatus.is5xxServerError()) {
            log.error("[{} {}] {} | {}", httpStatus.value(), errorCode, errorMessage,
                e.getAdditionalInfo(), e);
        } else {
            log.warn("[{} {}] {} | {}", httpStatus.value(), errorCode, errorMessage,
                e.getAdditionalInfo(), e);
        }

        return ErrorResponses.forBusiness(errorCode, instance(req));
    }

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

        log.warn("[400 BAD_REQUEST] Validation failed: {}", e.toString(), e);

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
        log.warn("[400 BAD_REQUEST] Illegal argument: {}", e.toString(), e);

        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            e.getMessage(),
            URI.create(req.getRequestURI())
        );
    }

    // 400: 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(
        MethodArgumentTypeMismatchException e,
        HttpServletRequest req
    ) {
        log.warn("[400 BAD_REQUEST] Type mismatch: {}", e.toString(), e);

        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            "파라미터 타입이 올바르지 않습니다: " + e.getName(),
            instance(req)
        );
    }

    // 400: JSON 파싱 불가
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(
        HttpMessageNotReadableException e,
        HttpServletRequest req
    ) {
        log.warn("[400 BAD_REQUEST] Not readable: {}", e.toString(), e);

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

        log.warn("[400 BAD_REQUEST] Missing parameter: {}", e.toString(), e);

        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            message,
            instance(req)
        );
    }

    // 400: 필수 요청 쿠키가 누락된 경우
    @ExceptionHandler(MissingRequestCookieException.class)
    public ProblemDetail handleMissingRequestCookie(
        MissingRequestCookieException e,
        HttpServletRequest req
    ) {
        String message = String.format("필수 쿠키 '%s'가 누락되었습니다.", e.getCookieName());

        log.warn("[400 BAD_REQUEST] Missing cookie: {}", e.toString(), e);

        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            message,
            instance(req)
        );
    }

    // 400: 필수 요청 헤더가 누락된 경우
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingRequestHeader(
        MissingRequestHeaderException e,
        HttpServletRequest req
    ) {
        String message = String.format("필수 헤더 '%s'가 누락되었습니다.", e.getHeaderName());

        log.warn("[400 BAD_REQUEST] Missing header: {}", e.toString(), e);

        return ErrorResponses.of(
            HttpStatus.BAD_REQUEST,
            message,
            instance(req)
        );
    }

    // 401: 컨트롤러/서비스 단에서 발생하는 JWT 관련 예외 처리
    // (주로 토큰 재발급 시 만료된 토큰을 파싱하려 할 때 발생)
    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleJwtExceptionInController(JwtException e, HttpServletRequest req) {
        log.warn("[401 UNAUTHORIZED] JWT exception: {}", e.toString(), e);

        return ErrorResponses.of(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않은 형식의 토큰입니다.",
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
        log.warn("[{} {}] {}", status.value(), status.getReasonPhrase(), e.toString(), e);

        return ErrorResponses.of(status, e.getMessage(), instance(req));
    }

    // 500: 그외 모든 예외
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleFallback(Exception e, HttpServletRequest req) {
        log.error("[500 UNEXPECTED] {}", e.toString(), e);

        return ErrorResponses.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            instance(req)
        );
    }
}
