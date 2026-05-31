package orm.annotation.validation;

import orm.core.validation.PatternValidator;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = PatternValidator.class)
public @interface Pattern {
    String message() default "Поле не соответствует шаблону {regexp}";
    String regexp();
}