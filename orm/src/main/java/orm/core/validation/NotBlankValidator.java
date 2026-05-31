package orm.core.validation;

import orm.annotation.validation.NotBlank;

public class NotBlankValidator implements ConstraintValidator<NotBlank, String> {
    @Override
    public boolean isValid(String value, NotBlank annotation) {
        if (value == null) return true;
        return !value.trim().isEmpty();
    }
}