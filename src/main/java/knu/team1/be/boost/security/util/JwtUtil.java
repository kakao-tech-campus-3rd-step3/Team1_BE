package knu.team1.be.boost.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.auth.dto.TokenDto;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.auth.exception.MissingAuthoritiesClaimException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class JwtUtil {

    private final Key key;

    private final Duration accessTokenExpireTime;
    private final Duration refreshTokenExpireTime;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtUtil(
        @Value("${jwt.secret}") String secretKey,
        @Value("${jwt.access-token-expire-time}") Duration accessTokenExpireTime,
        @Value("${jwt.refresh-token-expire-time}") Duration refreshTokenExpireTime
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
    }

    public TokenDto generateToken(Authentication userAuth) {
        String authorities = userAuth
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        UserPrincipalDto userPrincipalDto = (UserPrincipalDto) userAuth.getPrincipal();

        long now = (new Date()).getTime();

        String accessToken = Jwts.builder()
            .setSubject(userPrincipalDto.id().toString())
            .claim(AUTHORITIES_KEY, authorities)
            .claim("name", userPrincipalDto.name())
            .claim("avatar", userPrincipalDto.avatar())
            .setExpiration(new Date(now + accessTokenExpireTime.toMillis()))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        String refreshToken = Jwts.builder()
            .setSubject(userPrincipalDto.id().toString())
            .setExpiration(new Date(now + refreshTokenExpireTime.toMillis()))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        return TokenDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);
        if (authoritiesClaim == null) {
            throw new MissingAuthoritiesClaimException();
        }

        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY)
                    .toString()
                    .split(",")
                )
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        String name = claims.get("name", String.class);
        String avatar = claims.get("avatar", String.class);

        UserPrincipalDto userPrincipalDto = UserPrincipalDto.from(
            UUID.fromString(claims.getSubject()),
            name,
            avatar
        );
        return new UsernamePasswordAuthenticationToken(userPrincipalDto, "", authorities);
    }

    public void validateToken(String token) {
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            // 토큰 재발급에 필요
            return e.getClaims();
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        return resolveToken(bearerToken);
    }

    public String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
