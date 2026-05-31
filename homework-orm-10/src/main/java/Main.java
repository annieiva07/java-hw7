import model.RegistrationForm;
import orm.repository.DBConfig;
import orm.repository.EntityManager;
import orm.core.validation.ValidationException;
import orm.core.validation.Violation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DBConfig.getConnection()) {
            EntityManager em = new EntityManager(connection);

            // Создаём таблицу
            em.createTable(RegistrationForm.class);
            System.out.println("Таблица создана успешно");
            System.out.println();

            System.out.println("--- Сценарий 1: Сохранение валидной формы ---");
            RegistrationForm validForm = new RegistrationForm("ivan123", "password123", 25);

            try {
                Long id = em.save(validForm);
                System.out.println("✓ Валидная форма сохранена с ID: " + id);
                System.out.println("  Логин: " + validForm.getLogin());
                System.out.println("  Возраст: " + validForm.getAge());
            } catch (ValidationException e) {
                System.out.println("Ошибка валидации: " + e.getMessage());
            }
            System.out.println();

            System.out.println("--- Сценарий 2: Сохранение невалидной формы ---");
            RegistrationForm invalidForm = new RegistrationForm("a", "123", 15);

            try {
                em.save(invalidForm);
                System.out.println("Форма сохранилась (не должно было произойти)");
            } catch (ValidationException e) {
                System.out.println("✗ Ошибка валидации! Нарушения:");
                for (Violation violation : e.getViolations()) {
                    System.out.println("  - " + violation);
                }
            }
            System.out.println();

            System.out.println("--- Сценарий 3: saveAll с невалидной формой в пачке ---");

            RegistrationForm form1 = new RegistrationForm("user1", "password123", 20);
            RegistrationForm form2 = new RegistrationForm("ab", "pass", 17);  // НЕВАЛИДНАЯ
            RegistrationForm form3 = new RegistrationForm("user3", "password123", 30);

            try {
                em.saveAll(List.of(form1, form2, form3));
                System.out.println("Все формы сохранились (не должно было произойти)");
            } catch (ValidationException e) {
                System.out.println("✗ Транзакция откачена! Нарушения:");
                for (Violation violation : e.getViolations()) {
                    System.out.println("  - " + violation);
                }
                System.out.println("Ни одна форма не была сохранена в БД");
            }
            System.out.println();

            long count = em.count(RegistrationForm.class);
            System.out.println("--- Итог ---");
            System.out.println("Всего форм в БД: " + count);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}