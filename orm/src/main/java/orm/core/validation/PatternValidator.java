package orm.core.validation;

import orm.annotation.validation.Pattern;

public class PatternValidator implements ConstraintValidator<Pattern, String> {
    @Override
    public boolean isValid(String value, Pattern annotation) {
        if (value == null) return true;
        return value.matches(annotation.regexp());
    }
}