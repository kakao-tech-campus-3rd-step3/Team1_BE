package knu.team1.be.boost.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String additionalInfo; // 로그에 찍을 추가 정보

    public BusinessException(ErrorCode errorCode, String additionalInfo) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
        this.additionalInfo = additionalInfo;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
        this.additionalInfo = "";
    }

}
