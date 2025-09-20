package knu.team1.be.boost.auth.exception;

public class MissingAuthoritiesClaimException extends RuntimeException {

    public MissingAuthoritiesClaimException() {
        super("권한 정보가 없는 토큰입니다.");
    }
}
