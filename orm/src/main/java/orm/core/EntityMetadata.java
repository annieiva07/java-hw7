package orm.core;

import orm.annotation.*;
import orm.core.ColumnMetadata;

import java.lang.reflect.Field;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// класс хранит метаинформацию о классе
public class EntityMetadata {
    private final Class<?> entityClass; // класс сущности
    private final String tableName; // имя таблицы
    private final List<ColumnMetadata> columns; // список колонок
    private ColumnMetadata idColumn; // колонка-первичный ключ

    public EntityMetadata(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.columns = new ArrayList<>();

        // проверяем наличие аннотации @Table
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new OrmException("Класс " + entityClass.getName() + " не помечен аннотацией @Table");
        }

        // определяем имя таблицы
        String tableNameFromAnnotation = tableAnnotation.name();
        this.tableName = tableNameFromAnnotation.isEmpty()
                ? entityClass.getSimpleName().toLowerCase()
                : tableNameFromAnnotation;

        // обрабатываем все поля класса
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                processIdField(field);
            } else if (field.isAnnotationPresent(Column.class)) {
                processColumnField(field);
            }
        }

        // проверяем, что первичный ключ найден
        if (idColumn == null) {
            throw new OrmException("В классе " + entityClass.getName() + " не найдено поле с @Id");
        }
    }

    // обрабатывает поле-первичный ключ
    private void processIdField(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        String columnName = columnAnnotation != null && !columnAnnotation.name().isEmpty()
                ? columnAnnotation.name()
                : field.getName();

        Class<?> type = field.getType();
        if (type != Long.class && type != long.class) {
            throw new OrmException("Поле с @Id должно быть типа Long или long в классе " + entityClass.getName());
        }

        idColumn = new ColumnMetadata(field, columnName, true, false, Types.BIGINT);
        columns.add(idColumn);
    }

    // обрабатывает обычное поле с аннотацией @Column
    private void processColumnField(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        String columnName = columnAnnotation.name().isEmpty()
                ? field.getName()
                : columnAnnotation.name();

        int sqlType = getSqlType(field.getType());
        boolean nullable = columnAnnotation.nullable();

        columns.add(new ColumnMetadata(field, columnName, false, nullable, sqlType));
    }

    // преобразует Java-тип в SQL-тип
    private int getSqlType(Class<?> type) {
        if (type == Long.class || type == long.class) return Types.BIGINT;
        if (type == Integer.class || type == int.class) return Types.INTEGER;
        if (type == Double.class || type == double.class) return Types.DOUBLE;
        if (type == Boolean.class || type == boolean.class) return Types.BOOLEAN;
        if (type == String.class) return Types.VARCHAR;
        if (type == LocalDate.class) return Types.DATE;
        throw new OrmException("Неподдерживаемый тип поля: " + type.getName());
    }

    public String getTableName() { return tableName; }
    public List<ColumnMetadata> getColumns() { return columns; }
    public ColumnMetadata getIdColumn() { return idColumn; }

    // возвращает все колонки, кроме первичного ключа
    public List<ColumnMetadata> getNonIdColumns() {
        return columns.stream().filter(c -> !c.isId()).toList();
    }

    // генерирует SQL для создания таблицы
    public String generateCreateTableSQL() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName).append(" (");

        for (int i = 0; i < columns.size(); i++) {
            ColumnMetadata col = columns.get(i);
            sql.append(col.getColumnName()).append(" ");

            // определяем SQL-тип
            switch (col.getSqlType()) {
                case Types.BIGINT -> sql.append("BIGINT");
                case Types.INTEGER -> sql.append("INT");
                case Types.DOUBLE -> sql.append("DOUBLE");
                case Types.BOOLEAN -> sql.append("BOOLEAN");
                case Types.VARCHAR -> sql.append("VARCHAR(255)");
                case Types.DATE -> sql.append("DATE");
            }

            if (col.isId()) {
                sql.append(" AUTO_INCREMENT PRIMARY KEY");
            }

            if (!col.isNullable()) {
                sql.append(" NOT NULL");
            }

            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(")");
        return sql.toString();
    }

    // генерирует SQL для вставки записи
    public String generateInsertSQL() {
        List<ColumnMetadata> nonIdCols = getNonIdColumns();
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");

        for (int i = 0; i < nonIdCols.size(); i++) {
            sql.append(nonIdCols.get(i).getColumnName());
            if (i < nonIdCols.size() - 1) sql.append(", ");
        }

        sql.append(") VALUES (");
        for (int i = 0; i < nonIdCols.size(); i++) {
            sql.append("?");
            if (i < nonIdCols.size() - 1) sql.append(", ");
        }
        sql.append(")");

        return sql.toString();
    }
}