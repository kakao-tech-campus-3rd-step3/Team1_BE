package knu.team1.be.boost.common.exception;

import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class ErrorResponses {

    private ErrorResponses() {
    }

    /**
     * 기본 ProblemDetail을 생성
     *
     * @param status   상태 코드
     * @param detail   세부 정보
     * @param instance uri
     * @return problemDetail 객체
     */
    public static ProblemDetail of(HttpStatus status, String detail, URI instance) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setInstance(instance);
        // RFC 9457의 권고에 따라 type이 `about:blank`일 때 title을 HTTP status phrase와 동일하게 지정
        pd.setTitle(status.getReasonPhrase());
        return pd;
    }

    /**
     * property가 포함된 ProblemDetail을 생성
     *
     * @param status   상태 코드
     * @param detail   세부 정보
     * @param instance uri
     * @param props    기타 정보
     * @return problemDetail 객체
     */
    public static ProblemDetail of(HttpStatus status, String detail, URI instance,
        Map<String, ?> props
    ) {
        ProblemDetail pd = of(status, detail, instance);
        if (props != null) {
            props.forEach(pd::setProperty);
        }
        return pd;
    }

    /**
     * 비즈니스 예외를 위한 ProblemDetail을 생성. ErrorCode의 name을 type으로 설정하여 클라이언트에서 오류 타입을 식별할 수 있도록 함.
     *
     * @param errorCode 비즈니스 오류 코드
     * @param instance  uri
     * @return problemDetail 객체
     */
    public static ProblemDetail forBusiness(ErrorCode errorCode, URI instance) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            errorCode.getHttpStatus(),
            errorCode.getErrorMessage()
        );
        pd.setInstance(instance);
        pd.setTitle(errorCode.getHttpStatus().getReasonPhrase());
        pd.setType(URI.create("urn:problem:" + errorCode.name().toLowerCase()));
        return pd;
    }

}
