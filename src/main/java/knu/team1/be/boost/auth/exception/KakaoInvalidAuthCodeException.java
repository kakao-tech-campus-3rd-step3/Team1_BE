package knu.team1.be.boost.auth.exception;

public class KakaoInvalidAuthCodeException extends RuntimeException {

    public KakaoInvalidAuthCodeException() {
        super("인가 코드가 유효하지 않습니다.");
    }
}
