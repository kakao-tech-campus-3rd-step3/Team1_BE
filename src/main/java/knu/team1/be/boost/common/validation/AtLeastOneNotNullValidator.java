package knu.team1.be.boost.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class AtLeastOneNotNullValidator implements ConstraintValidator<AtLeastOneNotNull, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // DTO 자체가 null인 경우는 @NotNull로 잡아야 하므로 여기서는 통과
        }

        // 리플렉션을 사용하여 모든 필드를 순회
        for (Field field : value.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true); // private 필드에 접근 허용
                if (field.get(value) != null) {
                    return true; // null이 아닌 필드가 하나라도 있으면 유효
                }
            } catch (IllegalAccessException e) {
                // 실제로는 로그를 남기는 것이 좋음
                throw new RuntimeException("필드 값에 접근하는 중 오류 발생", e);
            }
        }

        return false; // 모든 필드가 null이면 유효하지 않음
    }
}
