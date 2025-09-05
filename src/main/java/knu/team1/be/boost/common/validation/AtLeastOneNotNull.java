package knu.team1.be.boost.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE}) // 클래스 또는 레코드 레벨에 붙일 어노테이션
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneNotNullValidator.class)
// 이 어노테이션의 검증 로직은 AtLeastOneNotNullValidator가 담당
public @interface AtLeastOneNotNull {

    String message() default "적어도 하나의 필드는 null이 아니어야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
