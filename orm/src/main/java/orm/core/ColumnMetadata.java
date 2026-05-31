package orm.core;

import java.lang.reflect.Field;

// Класс хранит метаинформацию о колонке таблицы
public class ColumnMetadata {
    private final Field field; // поле класса
    private final String columnName; // имя колонки в БД
    private final boolean isId;// является ли первичным ключом
    private final boolean nullable;// может ли быть NULL
    private final int sqlType;// тип в SQL

    public ColumnMetadata(Field field, String columnName, boolean isId, boolean nullable, int sqlType) {
        this.field = field;
        this.columnName = columnName;
        this.isId = isId;
        this.nullable = nullable;
        this.sqlType = sqlType;
        this.field.setAccessible(true);
    }

    public Field getField() { return field; }
    public String getColumnName() { return columnName; }
    public boolean isId() { return isId; }
    public boolean isNullable() { return nullable; }
    public int getSqlType() { return sqlType; }


    // получает значение поля из объекта
    public Object getValue(Object entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new OrmException("Не удалось получить значение поля", e);
        }
    }

    // устанавливает значение поля в объект
    public void setValue(Object entity, Object value) {
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new OrmException("Не удалось установить значение поля", e);
        }
    }
}