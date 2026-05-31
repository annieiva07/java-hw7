package orm.annotation.validation;

import orm.core.validation.MinValidator;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MinValidator.class)
public @interface Min {
    String message() default "Значение должно быть не меньше {value}";
    long value();
}