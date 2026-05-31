package orm.core.validation;

import orm.core.OrmException;
import java.util.ArrayList;
import java.util.List;

public class ValidationException extends OrmException {

    private final List<Violation> violations;

    public ValidationException(String message) {
        super(message);
        this.violations = new ArrayList<>();
    }

    public ValidationException(Violation violation) {
        super(violation.toString());
        this.violations = new ArrayList<>();
        this.violations.add(violation);
    }

    public ValidationException(List<Violation> violations) {
        super("Обнаружены нарушения валидации: " + violations.size() + " ошибок");
        this.violations = new ArrayList<>(violations);
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }
}