package knu.team1.be.boost.common.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITELIST = Arrays.asList(
        "/api/auth/login/kakao",
        "/api/auth/reissue"
    );

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (isWhitelisted(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtTokenProvider.resolveToken(request);

        // 토큰이 있는 경우에만 검증 및 인증 정보 설정
        if (StringUtils.hasText(token)) {
            // 예외가 발생하면 JwtExceptionFilter로 처리 위임
            jwtTokenProvider.validateToken(token);
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 토큰이 없는 경우, SecurityContext가 비어있는 상태로 다음 필터로 넘어감
        // 이후 Spring Security의 인가(Authorization) 필터가 인증이 필요한 경로에 대해
        // 인증 예외를 발생시키고, 이는 AuthenticationEntryPoint에서 처리됨
        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String requestURI) {
        return WHITELIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }
}
