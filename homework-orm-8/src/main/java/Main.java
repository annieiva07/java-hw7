import model.Student;
import orm.repository.DBConfig;
import orm.repository.EntityManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DBConfig.getConnection()) {
            EntityManager em = new EntityManager(connection);

            // Создаём таблицу
            em.createTable(Student.class);
            System.out.println("=== Таблица students создана ===");
            System.out.println();

            // ===== СЦЕНАРИЙ 1: Сохранение студентов с разными датами =====
            System.out.println("--- Сценарий 1: Сохранение 3 студентов с разными датами ---");

            Student student1 = new Student("Иван Иванов", 20, 4.5, LocalDate.of(2023, 9, 1));
            Student student2 = new Student("Петр Петров", 21, 4.2, LocalDate.of(2023, 9, 2));
            Student student3 = new Student("Мария Сидорова", 19, 4.8, LocalDate.of(2023, 9, 3));

            Long id1 = em.save(student1);
            Long id2 = em.save(student2);
            Long id3 = em.save(student3);

            System.out.println("Сохранён студент 1: " + student1);
            System.out.println("Сохранён студент 2: " + student2);
            System.out.println("Сохранён студент 3: " + student3);
            System.out.println();

            System.out.println("--- Сценарий 2: Поиск существующего студента по ID ---");

            Optional<Student> found = em.findById(Student.class, id2);
            if (found.isPresent()) {
                Student s = found.get();
                System.out.println("Найден студент с ID=" + id2 + ": " + s.getName());
                System.out.println("  Дата зачисления: " + s.getEnrollmentDate());
            } else {
                System.out.println("Студент не найден");
            }
            System.out.println();

            System.out.println("--- Сценарий 3: Получение всех студентов ---");

            List<Student> allStudents = em.findAll(Student.class);
            System.out.println("Всего студентов в БД: " + allStudents.size());
            for (Student s : allStudents) {
                System.out.println("  - " + s.getName() + " (поступил: " + s.getEnrollmentDate() + ")");
            }
            System.out.println();

            System.out.println("--- Сценарий 4: Поиск несуществующего ID ---");

            Optional<Student> notFound = em.findById(Student.class, 999L);
            if (notFound.isEmpty()) {
                System.out.println("Optional.empty() - студент с ID=999 не найден");
            } else {
                System.out.println("Студент найден (не должно было произойти)");
            }
            System.out.println();

            System.out.println("--- Поиск студентов по имени ---");

            List<Student> ivanStudents = em.findAllWhere(Student.class, "name", "Иван Иванов");
            System.out.println("Найдено студентов с именем 'Иван Иванов': " + ivanStudents.size());

            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}