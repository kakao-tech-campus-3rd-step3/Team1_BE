package knu.team1.be.boost.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.exception.ErrorResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {

        ErrorCode errorCode = ErrorCode.AUTHENTICATION_FAILED;
        HttpStatus httpStatus = errorCode.getHttpStatus();
        String errorMessage = errorCode.getErrorMessage();

        ProblemDetail problemDetail = ErrorResponses.forBusiness(
            ErrorCode.AUTHENTICATION_FAILED,
            URI.create(request.getRequestURI())
        );

        log.warn("[{} {}] {}", httpStatus.value(), errorCode, errorMessage, authException);

        setErrorResponse(response, problemDetail);
    }

    private void setErrorResponse(HttpServletResponse response, ProblemDetail problemDetail)
        throws IOException {
        response.setStatus(problemDetail.getStatus());

        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
