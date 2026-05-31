package orm.annotation.validation;

import orm.core.validation.NotBlankValidator;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = NotBlankValidator.class)
public @interface NotBlank {
    String message() default "Поле не может быть пустым";
}