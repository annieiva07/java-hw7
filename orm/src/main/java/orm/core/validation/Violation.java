package orm.core.validation;

import java.lang.annotation.Annotation;

public record Violation(
        String field,
        Object invalidValue,
        String message,
        Class<? extends Annotation> annotation
) {
    @Override
    public String toString() {
        return "Поле '" + field + "' = " + invalidValue + " - " + message;
    }
}