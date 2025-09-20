package knu.team1.be.boost.auth.exception;

public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException() {
        super("리프레시 토큰을 찾을 수 없습니다.");
    }
}
