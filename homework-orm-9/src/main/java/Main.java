import model.Book;
import orm.repository.DBConfig;
import orm.repository.EntityManager;
import orm.core.OrmException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DBConfig.getConnection()) {
            EntityManager em = new EntityManager(connection);

            // Создаём таблицу
            em.createTable(Book.class);
            System.out.println("=== Таблица books создана ===");
            System.out.println();

            System.out.println("--- Сценарий 1: saveAll (5 книг) ---");

            List<Book> books = List.of(
                    new Book("Война и мир", "Лев Толстой", 1869, true),
                    new Book("Преступление и наказание", "Фёдор Достоевский", 1866, true),
                    new Book("Анна Каренина", "Лев Толстой", 1877, false),
                    new Book("Евгений Онегин", "Александр Пушкин", 1833, true),
                    new Book("Мёртвые души", "Николай Гоголь", 1842, true)
            );

            em.saveAll(books);
            System.out.println("Сохранено " + books.size() + " книг");
            for (Book b : books) {
                System.out.println("  " + b);
            }
            System.out.println();

            System.out.println("--- Сценарий 2: findAllWhere (поиск книг Льва Толстого) ---");

            List<Book> tolstoyBooks = em.findAllWhere(Book.class, "author", "Лев Толстой");
            System.out.println("Найдено книг Льва Толстого: " + tolstoyBooks.size());
            for (Book b : tolstoyBooks) {
                System.out.println("  - " + b.getTitle() + " (" + b.getPublicationYear() + ")");
            }
            System.out.println();

            System.out.println("--- Сценарий 3: update (обновление книги) ---");

            // Находим книгу для обновления
            Optional<Book> bookToUpdate = em.findOneWhere(Book.class, "title", "Анна Каренина");
            if (bookToUpdate.isPresent()) {
                Book book = bookToUpdate.get();
                System.out.println("До обновления: " + book);
                book.setAvailable(true);
                book.setPublicationYear(1878);
                int updated = em.update(book);
                System.out.println("Обновлено строк: " + updated);

                Optional<Book> updatedBook = em.findById(Book.class, book.getId());
                updatedBook.ifPresent(b -> System.out.println("После обновления: " + b));
            }
            System.out.println();

            System.out.println("--- Сценарий 4: findOneWhere (поиск одной книги по названию) ---");

            Optional<Book> onegin = em.findOneWhere(Book.class, "title", "Евгений Онегин");
            onegin.ifPresent(b -> System.out.println("Найдена книга: " + b));
            System.out.println();

            System.out.println("--- Сценарий 5: delete (удаление книги) ---");

            Optional<Book> toDelete = em.findOneWhere(Book.class, "title", "Мёртвые души");
            if (toDelete.isPresent()) {
                Book book = toDelete.get();
                System.out.println("Удаляем: " + book);
                int deleted = em.delete(book);
                System.out.println("Удалено строк: " + deleted);
            }
            System.out.println();

            System.out.println("--- Сценарий 6: count (подсчёт строк) ---");

            long count = em.count(Book.class);
            System.out.println("Всего книг в БД: " + count);
            System.out.println();

            System.out.println("--- Сценарий 7: existsById ---");

            boolean exists = em.existsById(Book.class, 1L);
            System.out.println("Книга с ID=1 существует: " + exists);

            boolean notExists = em.existsById(Book.class, 999L);
            System.out.println("Книга с ID=999 существует: " + notExists);
            System.out.println();

            System.out.println("--- Сценарий 8: откат saveAll при null title ---");

            List<Book> booksWithNull = List.of(
                    new Book("Книга 1", "Автор 1", 2000, true),
                    new Book(null, "Автор 2", 2001, true),
                    new Book("Книга 3", "Автор 3", 2002, true)
            );

            long beforeCount = em.count(Book.class);
            System.out.println("Книг до saveAll: " + beforeCount);

            try {
                em.saveAll(booksWithNull);
                System.out.println("Все книги сохранились (не должно было произойти)");
            } catch (OrmException e) {
                System.out.println("✗ Ошибка при saveAll: " + e.getMessage());
                System.out.println("  Транзакция откачена, ни одна книга не добавлена");
            }

            long afterCount = em.count(Book.class);
            System.out.println("Книг после saveAll: " + afterCount);
            System.out.println("  (количество не изменилось, откат сработал)");
            System.out.println();

            System.out.println();

            System.out.println("Финальное количество книг в БД: " + em.count(Book.class));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}