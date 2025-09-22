package knu.team1.be.boost.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import knu.team1.be.boost.auth.exception.MissingAuthoritiesClaimException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.exception.ErrorResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException | MissingAuthoritiesClaimException e) {
            handleJwtException(request, response, e);
        }
    }

    private void handleJwtException(
        HttpServletRequest request,
        HttpServletResponse response,
        Exception e
    ) throws IOException {

        ErrorCode errorCode = resolveJwtErrorCode(e);
        HttpStatus httpStatus = errorCode.getHttpStatus();
        String errorMessage = errorCode.getErrorMessage();

        ProblemDetail problemDetail = ErrorResponses.forBusiness(
            errorCode,
            URI.create(request.getRequestURI())
        );

        log.warn("[{} {}] {}", httpStatus.value(), errorCode, errorMessage, e);

        setErrorResponse(response, problemDetail);
    }

    private ErrorCode resolveJwtErrorCode(Exception e) {
        if (e instanceof ExpiredJwtException) {
            return ErrorCode.TOKEN_EXPIRED;
        }
        if (e instanceof MalformedJwtException || e instanceof SecurityException) {
            return ErrorCode.INVALID_SIGNATURE;
        }
        if (e instanceof UnsupportedJwtException) {
            return ErrorCode.UNSUPPORTED_TOKEN;
        }
        if (e instanceof MissingAuthoritiesClaimException) {
            return ErrorCode.MISSING_CLAIMS;
        }
        return ErrorCode.AUTHENTICATION_FAILED;
    }

    private void setErrorResponse(HttpServletResponse response, ProblemDetail problemDetail)
        throws IOException {
        response.setStatus(problemDetail.getStatus());

        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
