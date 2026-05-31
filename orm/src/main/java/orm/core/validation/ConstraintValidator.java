package orm.core.validation;

import java.lang.annotation.Annotation;

public interface ConstraintValidator<A extends Annotation, T> {
    boolean isValid(T value, A annotation);
}