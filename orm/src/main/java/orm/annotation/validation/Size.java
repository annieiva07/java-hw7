package orm.annotation.validation;

import orm.core.validation.SizeValidator;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = SizeValidator.class)
public @interface Size {
    String message() default "Длина поля должна быть от {min} до {max}";
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}