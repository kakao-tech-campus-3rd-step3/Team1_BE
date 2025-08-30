package knu.team1.be.boost.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private URI instance(HttpServletRequest req) {
        return URI.create(req.getRequestURI());
    }

//    // 같은 유형의 예외는 아래와 같이 일괄 처리

//    // 404: 도메인 NotFound
//    @ExceptionHandler({
//        UserNotFoundException.class,
//        TodoNotFoundException.class
//    })
//    public ProblemDetail handleNotFound(RuntimeException e, HttpServletRequest req) {
//        return ErrorResponses.of(HttpStatus.NOT_FOUND, e.getMessage(), instance(req));
//    }

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
