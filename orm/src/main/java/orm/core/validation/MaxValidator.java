package orm.core.validation;

import orm.annotation.validation.Max;

public class MaxValidator implements ConstraintValidator<Max, Number> {
    @Override
    public boolean isValid(Number value, Max annotation) {
        if (value == null) return true;
        return value.longValue() <= annotation.value();
    }
}