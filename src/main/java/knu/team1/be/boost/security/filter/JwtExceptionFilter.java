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
import java.util.Map;
import knu.team1.be.boost.auth.exception.MissingAuthoritiesClaimException;
import knu.team1.be.boost.common.exception.ErrorResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

        String errorCode = "INVALID_TOKEN";
        String message = "유효하지 않은 토큰입니다.";
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        if (e instanceof ExpiredJwtException) {
            errorCode = "TOKEN_EXPIRED";
            message = "만료된 토큰입니다.";
        } else if (e instanceof MalformedJwtException
            || e instanceof io.jsonwebtoken.security.SecurityException) {
            errorCode = "INVALID_SIGNATURE";
            message = "잘못된 서명이거나 유효하지 않은 형식의 토큰입니다.";
        } else if (e instanceof UnsupportedJwtException) {
            errorCode = "UNSUPPORTED_TOKEN";
            message = "지원하지 않는 형식의 토큰입니다.";
        } else if (e instanceof MissingAuthoritiesClaimException) {
            errorCode = "MISSING_CLAIMS";
            message = e.getMessage();
            status = HttpStatus.BAD_REQUEST; // 내용물 부재이기 때문에 400
        }

        // ErrorResponses를 사용하여 ProblemDetail 객체 생성
        ProblemDetail problemDetail = ErrorResponses.of(
            status,
            message,
            URI.create(request.getRequestURI()),
            Map.of("errorCode", errorCode) // 커스텀 프로퍼티 추가
        );

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
