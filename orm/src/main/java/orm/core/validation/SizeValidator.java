package orm.core.validation;

import orm.annotation.validation.Size;

public class SizeValidator implements ConstraintValidator<Size, String> {
    @Override
    public boolean isValid(String value, Size annotation) {
        if (value == null) return true;
        int len = value.length();
        return len >= annotation.min() && len <= annotation.max();
    }
}