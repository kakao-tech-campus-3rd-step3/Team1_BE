package knu.team1.be.boost.auth.exception;

import knu.team1.be.boost.common.exception.ErrorCode;

public class MissingAuthoritiesClaimException extends RuntimeException {

    public MissingAuthoritiesClaimException() {
        super(ErrorCode.MISSING_CLAIMS.getClientMessage());
    }
}
