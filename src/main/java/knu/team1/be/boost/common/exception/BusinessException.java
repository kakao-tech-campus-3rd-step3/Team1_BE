package knu.team1.be.boost.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String logMessage;

    public BusinessException(ErrorCode errorCode, String logMessage) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
        this.logMessage = logMessage;
    }

}
