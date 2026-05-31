package orm.annotation.validation;

import orm.core.validation.MaxValidator;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MaxValidator.class)
public @interface Max {
    String message() default "Значение должно быть не больше {value}";
    long value();
}