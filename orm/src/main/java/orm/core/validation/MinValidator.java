package orm.core.validation;

import orm.annotation.validation.Min;

public class MinValidator implements ConstraintValidator<Min, Number> {
    @Override
    public boolean isValid(Number value, Min annotation) {
        if (value == null) return true;
        return value.longValue() >= annotation.value();
    }
}