package knu.team1.be.boost.auth.exception;

public class RefreshTokenNotEqualsException extends RuntimeException {

    public RefreshTokenNotEqualsException() {
        super("리프레시 토큰이 일치하지 않습니다.");
    }
}
