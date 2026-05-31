package orm.repository;

import orm.core.validation.Validator;
import orm.core.validation.ValidationException;
import orm.core.validation.Violation;

import orm.core.EntityMetadata;
import orm.core.ColumnMetadata;
import orm.core.OrmException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

public class EntityManager {
    private final Connection connection;
    private final Map<Class<?>, EntityMetadata> metadataCache = new HashMap<>();

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    private EntityMetadata getMetadata(Class<?> clazz) {
        return metadataCache.computeIfAbsent(clazz, EntityMetadata::new);
    }

    public void createTable(Class<?> clazz) {
        EntityMetadata metadata = getMetadata(clazz);
        String sql = metadata.generateCreateTableSQL();

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new OrmException("Не удалось создать таблицу", e);
        }
    }

    // сохраняет объект в базу данных с валидацией
    public Long save(Object entity) {
        Validator.validateOrThrow(entity);

        EntityMetadata metadata = getMetadata(entity.getClass());
        String sql = metadata.generateInsertSQL();

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            List<ColumnMetadata> nonIdCols = metadata.getNonIdColumns();
            for (int i = 0; i < nonIdCols.size(); i++) {
                Object value = nonIdCols.get(i).getValue(entity);
                setParameter(pstmt, i + 1, value, nonIdCols.get(i).getSqlType());
            }

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    metadata.getIdColumn().setValue(entity, id);
                    return id;
                } else {
                    throw new OrmException("Не удалось получить сгенерированный ID");
                }
            }
        } catch (SQLException e) {
            throw new OrmException("Не удалось сохранить сущность", e);
        }
    }

    // находит сущность по ID
    public <T> Optional<T> findById(Class<T> clazz, Long id) {
        EntityMetadata metadata = getMetadata(clazz);
        String sql = "SELECT * FROM " + metadata.getTableName() +
                " WHERE " + metadata.getIdColumn().getColumnName() + " = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                T entity = clazz.getDeclaredConstructor().newInstance();
                for (ColumnMetadata col : metadata.getColumns()) {
                    Object value = rs.getObject(col.getColumnName());
                    if (col.getSqlType() == Types.DATE && value instanceof Date) {
                        value = ((Date) value).toLocalDate();
                    }
                    col.setValue(entity, value);
                }
                return Optional.of(entity);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new OrmException("Не удалось найти сущность по ID", e);
        }
    }

    // возвращает все сущности из таблицы
    public <T> List<T> findAll(Class<T> clazz) {
        EntityMetadata metadata = getMetadata(clazz);
        String sql = "SELECT * FROM " + metadata.getTableName();
        List<T> results = new ArrayList<>();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                T entity = clazz.getDeclaredConstructor().newInstance();
                for (ColumnMetadata col : metadata.getColumns()) {
                    Object value = rs.getObject(col.getColumnName());
                    if (col.getSqlType() == Types.DATE && value instanceof Date) {
                        value = ((Date) value).toLocalDate();
                    }
                    col.setValue(entity, value);
                }
                results.add(entity);
            }
            return results;
        } catch (Exception e) {
            throw new OrmException("Не удалось получить все сущности", e);
        }
    }

    private void setParameter(PreparedStatement pstmt, int index, Object value, int sqlType) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, sqlType);
            return;
        }

        switch (sqlType) {
            case Types.DATE -> {
                if (value instanceof LocalDate) {
                    pstmt.setDate(index, Date.valueOf((LocalDate) value));
                } else {
                    pstmt.setObject(index, value);
                }
            }
            default -> pstmt.setObject(index, value);
        }
    }

    // обновляет все колонки, кроме id с валидацией
    public int update(Object entity) {
        Validator.validateOrThrow(entity);

        EntityMetadata metadata = getMetadata(entity.getClass());

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(metadata.getTableName()).append(" SET ");

        List<ColumnMetadata> nonIdCols = metadata.getNonIdColumns();
        for (int i = 0; i < nonIdCols.size(); i++) {
            sql.append(nonIdCols.get(i).getColumnName()).append(" = ?");
            if (i < nonIdCols.size() - 1) sql.append(", ");
        }

        sql.append(" WHERE ").append(metadata.getIdColumn().getColumnName()).append(" = ?");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < nonIdCols.size(); i++) {
                Object value = nonIdCols.get(i).getValue(entity);
                setParameter(pstmt, i + 1, value, nonIdCols.get(i).getSqlType());
            }

            Object idValue = metadata.getIdColumn().getValue(entity);
            setParameter(pstmt, nonIdCols.size() + 1, idValue, metadata.getIdColumn().getSqlType());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("Не удалось обновить сущность", e);
        }
    }

    public int delete(Object entity) {
        EntityMetadata metadata = getMetadata(entity.getClass());
        Object id = metadata.getIdColumn().getValue(entity);
        return deleteById(entity.getClass(), (Long) id);
    }

    public int deleteById(Class<?> clazz, Long id) {
        EntityMetadata metadata = getMetadata(clazz);
        String sql = "DELETE FROM " + metadata.getTableName() +
                " WHERE " + metadata.getIdColumn().getColumnName() + " = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("Не удалось удалить сущность", e);
        }
    }

    // сохраняет несколько объектов одной пачкой с валидацией всей пачки
    public void saveAll(List<?> entities) {
        if (entities == null || entities.isEmpty()) return;

        List<Violation> allViolations = new ArrayList<>();
        for (Object entity : entities) {
            List<Violation> violations = Validator.validate(entity);
            allViolations.addAll(violations);
        }
        if (!allViolations.isEmpty()) {
            throw new ValidationException(allViolations);
        }

        Class<?> firstClass = entities.get(0).getClass();
        for (Object entity : entities) {
            if (!firstClass.equals(entity.getClass())) {
                throw new OrmException("Все объекты в saveAll должны быть одного класса");
            }
        }

        EntityMetadata metadata = getMetadata(firstClass);
        String sql = metadata.generateInsertSQL();

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                List<ColumnMetadata> nonIdCols = metadata.getNonIdColumns();
                for (Object entity : entities) {
                    for (int i = 0; i < nonIdCols.size(); i++) {
                        Object value = nonIdCols.get(i).getValue(entity);
                        setParameter(pstmt, i + 1, value, nonIdCols.get(i).getSqlType());
                    }
                    pstmt.addBatch();
                }

                pstmt.executeBatch();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    for (Object entity : entities) {
                        if (generatedKeys.next()) {
                            Long id = generatedKeys.getLong(1);
                            metadata.getIdColumn().setValue(entity, id);
                        }
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new OrmException("Ошибка при batch-вставке, транзакция откачена", e);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new OrmException("Не удалось выполнить batch-вставку", e);
        }
    }

    // ищет все объекты, где поле равно значению
    public <T> List<T> findAllWhere(Class<T> clazz, String fieldName, Object value) {
        EntityMetadata metadata = getMetadata(clazz);

        ColumnMetadata targetColumn = null;
        for (ColumnMetadata col : metadata.getColumns()) {
            if (col.getField().getName().equals(fieldName)) {
                targetColumn = col;
                break;
            }
        }

        if (targetColumn == null) {
            throw new OrmException("Поле " + fieldName + " не найдено в классе " + clazz.getName());
        }

        String sql = "SELECT * FROM " + metadata.getTableName() +
                " WHERE " + targetColumn.getColumnName() + " = ?";
        List<T> results = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameter(pstmt, 1, value, targetColumn.getSqlType());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                T entity = clazz.getDeclaredConstructor().newInstance();
                for (ColumnMetadata col : metadata.getColumns()) {
                    Object val = rs.getObject(col.getColumnName());
                    if (col.getSqlType() == Types.DATE && val instanceof Date) {
                        val = ((Date) val).toLocalDate();
                    }
                    col.setValue(entity, val);
                }
                results.add(entity);
            }
            return results;
        } catch (Exception e) {
            throw new OrmException("Не удалось выполнить поиск по полю " + fieldName, e);
        }
    }

    // ищет один объект, где поле равно значению
    public <T> Optional<T> findOneWhere(Class<T> clazz, String fieldName, Object value) {
        List<T> results = findAllWhere(clazz, fieldName, value);

        if (results.size() > 1) {
            throw new OrmException("Найдено больше одной записи для поля " + fieldName + " со значением " + value);
        }

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // подсчитывает количество записей в таблице
    public long count(Class<?> clazz) {
        EntityMetadata metadata = getMetadata(clazz);
        String sql = "SELECT COUNT(*) FROM " + metadata.getTableName();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new OrmException("Не удалось подсчитать записи", e);
        }
    }

    // проверяет, существует ли запись с таким ID
    public boolean existsById(Class<?> clazz, Long id) {
        EntityMetadata metadata = getMetadata(clazz);
        String sql = "SELECT 1 FROM " + metadata.getTableName() +
                " WHERE " + metadata.getIdColumn().getColumnName() + " = ? LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new OrmException("Не удалось проверить существование записи", e);
        }
    }
}