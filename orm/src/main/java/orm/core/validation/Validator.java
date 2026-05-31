package orm.core.validation;

import orm.annotation.validation.Constraint;
import orm.annotation.validation.Size;
import orm.annotation.validation.Min;
import orm.annotation.validation.Max;
import orm.annotation.validation.Pattern;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class Validator {

    private static final Map<Class<?>, List<ValidationRule>> validationCache = new HashMap<>();

    // вспомогательный класс для хранения правил валидации
    private static class ValidationRule {
        private final Field field;
        private final Annotation annotation;
        private final ConstraintValidator<Annotation, Object> validator;

        @SuppressWarnings("unchecked")
        ValidationRule(Field field, Annotation annotation) {
            this.field = field;
            this.annotation = annotation;
            this.field.setAccessible(true);

            Constraint constraint = annotation.annotationType().getAnnotation(Constraint.class);
            if (constraint == null) {
                throw new RuntimeException("Аннотация " + annotation.annotationType().getName() + " не помечена @Constraint");
            }
            Class<?> validatorClass = constraint.validatedBy();
            try {
                this.validator = (ConstraintValidator<Annotation, Object>) validatorClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Не удалось создать валидатор для " + annotation.annotationType().getName(), e);
            }
        }

        Violation validate(Object target) {
            try {
                Object value = field.get(target);
                if (!validator.isValid(value, annotation)) {
                    String message = extractMessage(annotation);
                    return new Violation(field.getName(), value, message, annotation.annotationType());
                }
                return null;
            } catch (IllegalAccessException e) {
                return new Violation(field.getName(), null, "Не удалось получить значение поля", annotation.annotationType());
            }
        }

        private String extractMessage(Annotation annotation) {
            try {
                java.lang.reflect.Method messageMethod = annotation.annotationType().getMethod("message");
                String message = (String) messageMethod.invoke(annotation);
                message = replacePlaceholders(annotation, message);
                return message;
            } catch (Exception e) {
                return "Нарушено ограничение " + annotation.annotationType().getSimpleName();
            }
        }

        private String replacePlaceholders(Annotation annotation, String message) {
            if (annotation instanceof Size size) {
                message = message.replace("{min}", String.valueOf(size.min()));
                message = message.replace("{max}", String.valueOf(size.max()));
            } else if (annotation instanceof Min minVal) {
                message = message.replace("{value}", String.valueOf(minVal.value()));
            } else if (annotation instanceof Max maxVal) {
                message = message.replace("{value}", String.valueOf(maxVal.value()));
            } else if (annotation instanceof Pattern pattern) {
                message = message.replace("{regexp}", pattern.regexp());
            }
            return message;
        }
    }

    private static List<ValidationRule> getValidationRules(Class<?> clazz) {
        return validationCache.computeIfAbsent(clazz, cl -> {
            List<ValidationRule> rules = new ArrayList<>();
            for (Field field : cl.getDeclaredFields()) {
                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(Constraint.class)) {
                        rules.add(new ValidationRule(field, annotation));
                    }
                }
            }
            return rules;
        });
    }

    public static List<Violation> validate(Object object) {
        if (object == null) {
            return Collections.emptyList();
        }

        List<Violation> violations = new ArrayList<>();
        List<ValidationRule> rules = getValidationRules(object.getClass());

        for (ValidationRule rule : rules) {
            Violation violation = rule.validate(object);
            if (violation != null) {
                violations.add(violation);
            }
        }

        return violations;
    }

    public static void validateOrThrow(Object object) {
        List<Violation> violations = validate(object);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }
}