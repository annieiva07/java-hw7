import model.Student;
import orm.repository.DBConfig;
import orm.repository.EntityManager;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DBConfig.getConnection()) {
            EntityManager em = new EntityManager(connection);

            em.createTable(Student.class);
            System.out.println("Таблица успешно создана");

            Student student1 = new Student("Иван Иванов", 20, 4.5);
            Student student2 = new Student("Петр Петров", 21, 4.2);
            Student student3 = new Student("Мария Сидорова", 19, 4.8);

            Long id1 = em.save(student1);
            Long id2 = em.save(student2);
            Long id3 = em.save(student3);

            System.out.println("Сгенерированные ID:");
            System.out.println("Студент 1 ID: " + id1);
            System.out.println("Студент 2 ID: " + id2);
            System.out.println("Студент 3 ID: " + id3);

            System.out.println("\n--- Тестирование findById ---");
            var found = em.findById(Student.class, id1);
            found.ifPresent(s -> System.out.println("Найден: " + s.getName()));

            System.out.println("\n--- Тестирование findAll ---");
            var all = em.findAll(Student.class);
            all.forEach(s -> System.out.println("Студент: " + s.getName() + ", Возраст: " + s.getAge()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}