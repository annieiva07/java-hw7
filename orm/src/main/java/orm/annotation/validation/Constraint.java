package orm.annotation.validation;

import orm.core.validation.ConstraintValidator;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Constraint {
    Class<? extends ConstraintValidator<?, ?>> validatedBy();
}