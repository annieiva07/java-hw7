package orm.core.validation;

import orm.annotation.validation.NotNull;

public class NotNullValidator implements ConstraintValidator<NotNull, Object> {
    @Override
    public boolean isValid(Object value, NotNull annotation) {
        return value != null;
    }
}