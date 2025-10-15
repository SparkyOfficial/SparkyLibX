package com.sparky.libx.database.orm;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.sparky.libx.database.DatabaseManager;

/**
 * простий ORM фреймворк для роботи з базами даних
 * @author Андрій Будильников
 */
public class ORMFramework {
    
    private final DatabaseManager databaseManager;
    private final Map<Class<?>, EntityInfo> entityCache = new ConcurrentHashMap<>();
    
    public ORMFramework(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * анотація для позначення класу як сутності
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Entity {
        String table() default "";
    }
    
    /**
     * анотація для позначення поля як колонки
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Column {
        String name() default "";
        boolean primaryKey() default false;
        boolean autoIncrement() default false;
        boolean nullable() default true;
    }
    
    /**
     * анотація для ігнорування поля
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Transient {
    }
    
    /**
     * інформація про сутність
     */
    private static class EntityInfo {
        final String tableName;
        final List<FieldInfo> fields;
        final FieldInfo primaryKey;
        
        EntityInfo(String tableName, List<FieldInfo> fields, FieldInfo primaryKey) {
            this.tableName = tableName;
            this.fields = fields;
            this.primaryKey = primaryKey;
        }
    }
    
    /**
     * інформація про поле
     */
    private static class FieldInfo {
        final Field field;
        final String columnName;
        final boolean isPrimaryKey;
        final boolean isAutoIncrement;
        final boolean isNullable;
        
        FieldInfo(Field field, String columnName, boolean isPrimaryKey, boolean isAutoIncrement, boolean isNullable) {
            this.field = field;
            this.columnName = columnName;
            this.isPrimaryKey = isPrimaryKey;
            this.isAutoIncrement = isAutoIncrement;
            this.isNullable = isNullable;
            field.setAccessible(true);
        }
    }
    
    /**
     * зберегти сутність в базі даних
     * @param entity сутність
     */
    public <T> void save(T entity) throws SQLException {
        Class<?> clazz = entity.getClass();
        EntityInfo entityInfo = getEntityInfo(clazz);
        
        // перевірити чи є первинний ключ
        if (entityInfo.primaryKey != null) {
            // спробувати отримати значення первинного ключа
            try {
                Object primaryKeyValue = entityInfo.primaryKey.field.get(entity);
                if (primaryKeyValue != null) {
                    // оновити існуючу сутність
                    update(entity, entityInfo);
                    return;
                }
            } catch (IllegalAccessException e) {
                throw new SQLException("Не вдалося отримати значення первинного ключа", e);
            }
        }
        
        // вставити нову сутність
        insert(entity, entityInfo);
    }
    
    /**
     * вставити нову сутність
     */
    private <T> void insert(T entity, EntityInfo entityInfo) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        
        for (FieldInfo fieldInfo : entityInfo.fields) {
            // пропустити автоінкрементні поля
            if (fieldInfo.isAutoIncrement) {
                continue;
            }
            
            try {
                Object value = fieldInfo.field.get(entity);
                columnNames.add(fieldInfo.columnName);
                values.add(value);
                placeholders.add("?");
            } catch (IllegalAccessException e) {
                throw new SQLException("Не вдалося отримати значення поля " + fieldInfo.field.getName(), e);
            }
        }
        
        String sql = "INSERT INTO " + entityInfo.tableName + 
                    " (" + String.join(", ", columnNames) + ")" +
                    " VALUES (" + String.join(", ", placeholders) + ")";
        
        int rowsAffected = databaseManager.executeUpdate(sql, values.toArray());
        if (rowsAffected == 0) {
            throw new SQLException("Не вдалося вставити сутність");
        }
    }
    
    /**
     * оновити існуючу сутність
     */
    private <T> void update(T entity, EntityInfo entityInfo) throws SQLException {
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        Object primaryKeyValue = null;
        
        for (FieldInfo fieldInfo : entityInfo.fields) {
            // пропустити первинний ключ в SET частині
            if (fieldInfo.isPrimaryKey) {
                try {
                    primaryKeyValue = fieldInfo.field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new SQLException("Не вдалося отримати значення первинного ключа", e);
                }
                continue;
            }
            
            try {
                Object value = fieldInfo.field.get(entity);
                setClauses.add(fieldInfo.columnName + " = ?");
                values.add(value);
            } catch (IllegalAccessException e) {
                throw new SQLException("Не вдалося отримати значення поля " + fieldInfo.field.getName(), e);
            }
        }
        
        // додати значення первинного ключа в кінець
        values.add(primaryKeyValue);
        
        String sql = "UPDATE " + entityInfo.tableName + 
                    " SET " + String.join(", ", setClauses) +
                    " WHERE " + entityInfo.primaryKey.columnName + " = ?";
        
        int rowsAffected = databaseManager.executeUpdate(sql, values.toArray());
        if (rowsAffected == 0) {
            throw new SQLException("Не вдалося оновити сутність з ID " + primaryKeyValue);
        }
    }
    
    /**
     * знайти сутність за первинним ключем
     * @param clazz клас сутності
     * @param id значення первинного ключа
     * @return сутність або null
     */
    public <T> T findById(Class<T> clazz, Object id) throws SQLException {
        EntityInfo entityInfo = getEntityInfo(clazz);
        
        if (entityInfo.primaryKey == null) {
            throw new SQLException("Сутність " + clazz.getName() + " не має первинного ключа");
        }
        
        String sql = "SELECT * FROM " + entityInfo.tableName + 
                    " WHERE " + entityInfo.primaryKey.columnName + " = ?";
        
        List<Map<String, Object>> results = databaseManager.executeQuery(sql, id);
        if (results.isEmpty()) {
            return null;
        }
        
        return mapResultSetToEntity(results.get(0), clazz, entityInfo);
    }
    
    /**
     * знайти всі сутності
     * @param clazz клас сутності
     * @return список сутностей
     */
    public <T> List<T> findAll(Class<T> clazz) throws SQLException {
        EntityInfo entityInfo = getEntityInfo(clazz);
        
        String sql = "SELECT * FROM " + entityInfo.tableName;
        
        List<Map<String, Object>> results = databaseManager.executeQuery(sql);
        List<T> entities = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            entities.add(mapResultSetToEntity(row, clazz, entityInfo));
        }
        
        return entities;
    }
    
    /**
     * знайти сутності за умовою
     * @param clazz клас сутності
     * @param whereClause умова WHERE
     * @param parameters параметри
     * @return список сутностей
     */
    public <T> List<T> findByCondition(Class<T> clazz, String whereClause, Object... parameters) throws SQLException {
        EntityInfo entityInfo = getEntityInfo(clazz);
        
        String sql = "SELECT * FROM " + entityInfo.tableName + " WHERE " + whereClause;
        
        List<Map<String, Object>> results = databaseManager.executeQuery(sql, parameters);
        List<T> entities = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            entities.add(mapResultSetToEntity(row, clazz, entityInfo));
        }
        
        return entities;
    }
    
    /**
     * видалити сутність
     * @param entity сутність
     */
    public <T> void delete(T entity) throws SQLException {
        Class<?> clazz = entity.getClass();
        EntityInfo entityInfo = getEntityInfo(clazz);
        
        if (entityInfo.primaryKey == null) {
            throw new SQLException("Сутність " + clazz.getName() + " не має первинного ключа");
        }
        
        try {
            Object primaryKeyValue = entityInfo.primaryKey.field.get(entity);
            
            String sql = "DELETE FROM " + entityInfo.tableName + 
                        " WHERE " + entityInfo.primaryKey.columnName + " = ?";
            
            int rowsAffected = databaseManager.executeUpdate(sql, primaryKeyValue);
            if (rowsAffected == 0) {
                throw new SQLException("Не вдалося видалити сутність з ID " + primaryKeyValue);
            }
        } catch (IllegalAccessException e) {
            throw new SQLException("Не вдалося отримати значення первинного ключа", e);
        }
    }
    
    /**
     * видалити сутність за первинним ключем
     * @param clazz клас сутності
     * @param id значення первинного ключа
     */
    public <T> void deleteById(Class<T> clazz, Object id) throws SQLException {
        EntityInfo entityInfo = getEntityInfo(clazz);
        
        if (entityInfo.primaryKey == null) {
            throw new SQLException("Сутність " + clazz.getName() + " не має первинного ключа");
        }
        
        String sql = "DELETE FROM " + entityInfo.tableName + 
                    " WHERE " + entityInfo.primaryKey.columnName + " = ?";
        
        int rowsAffected = databaseManager.executeUpdate(sql, id);
        if (rowsAffected == 0) {
            throw new SQLException("Не вдалося видалити сутність з ID " + id);
        }
    }
    
    /**
     * отримати інформацію про сутність
     */
    private EntityInfo getEntityInfo(Class<?> clazz) {
        return entityCache.computeIfAbsent(clazz, this::createEntityInfo);
    }
    
    /**
     * створити інформацію про сутність
     */
    private EntityInfo createEntityInfo(Class<?> clazz) {
        Entity entityAnnotation = clazz.getAnnotation(Entity.class);
        String tableName = (entityAnnotation != null && !entityAnnotation.table().isEmpty()) 
                          ? entityAnnotation.table() 
                          : clazz.getSimpleName().toLowerCase();
        
        List<FieldInfo> fields = new ArrayList<>();
        FieldInfo primaryKey = null;
        
        // отримати всі поля класу та його суперкласів
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                // пропустити статичні поля та поля з анотацією @Transient
                if (Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(Transient.class)) {
                    continue;
                }
                
                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName;
                boolean isPrimaryKey = false;
                boolean isAutoIncrement = false;
                boolean isNullable = true;
                
                if (columnAnnotation != null) {
                    columnName = columnAnnotation.name().isEmpty() 
                                ? field.getName() 
                                : columnAnnotation.name();
                    isPrimaryKey = columnAnnotation.primaryKey();
                    isAutoIncrement = columnAnnotation.autoIncrement();
                    isNullable = columnAnnotation.nullable();
                } else {
                    // якщо немає анотації @Column, використовуємо ім'я поля
                    columnName = field.getName();
                }
                
                FieldInfo fieldInfo = new FieldInfo(field, columnName, isPrimaryKey, isAutoIncrement, isNullable);
                fields.add(fieldInfo);
                
                if (isPrimaryKey) {
                    primaryKey = fieldInfo;
                }
            }
        }
        
        return new EntityInfo(tableName, fields, primaryKey);
    }
    
    /**
     * перетворити результат запиту в сутність
     */
    @SuppressWarnings("unchecked")
    private <T> T mapResultSetToEntity(Map<String, Object> row, Class<T> clazz, EntityInfo entityInfo) throws SQLException {
        try {
            T entity = clazz.getDeclaredConstructor().newInstance();
            
            for (FieldInfo fieldInfo : entityInfo.fields) {
                Object value = row.get(fieldInfo.columnName);
                if (value != null) {
                    // конвертувати типи якщо потрібно
                    value = convertValue(value, fieldInfo.field.getType());
                    fieldInfo.field.set(entity, value);
                }
            }
            
            return entity;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new SQLException("Не вдалося створити екземпляр сутності " + clazz.getName(), e);
        }
    }
    
    /**
     * конвертувати значення до потрібного типу
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        // конвертація числових типів
        if (value instanceof Number) {
            Number number = (Number) value;
            if (targetType == int.class || targetType == Integer.class) {
                return number.intValue();
            } else if (targetType == long.class || targetType == Long.class) {
                return number.longValue();
            } else if (targetType == double.class || targetType == Double.class) {
                return number.doubleValue();
            } else if (targetType == float.class || targetType == Float.class) {
                return number.floatValue();
            } else if (targetType == short.class || targetType == Short.class) {
                return number.shortValue();
            } else if (targetType == byte.class || targetType == Byte.class) {
                return number.byteValue();
            }
        }
        
        // конвертація строкових типів
        if (value instanceof String) {
            String str = (String) value;
            if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(str);
            }
        }
        
        // якщо не вдалося конвертувати, повертаємо оригінальне значення
        return value;
    }
    
    /**
     * створити таблицю для сутності
     * @param clazz клас сутності
     */
    public <T> void createTable(Class<T> clazz) throws SQLException {
        EntityInfo entityInfo = getEntityInfo(clazz);
        
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(entityInfo.tableName).append(" (");
        
        List<String> columnDefinitions = new ArrayList<>();
        String primaryKeyDefinition = null;
        
        for (FieldInfo fieldInfo : entityInfo.fields) {
            StringBuilder columnDef = new StringBuilder();
            columnDef.append(fieldInfo.columnName).append(" ");
            
            // визначити тип колонки на основі типу поля
            String columnType = getColumnType(fieldInfo.field.getType());
            columnDef.append(columnType);
            
            // додати обмеження
            if (!fieldInfo.isNullable) {
                columnDef.append(" NOT NULL");
            }
            
            if (fieldInfo.isPrimaryKey) {
                columnDef.append(" PRIMARY KEY");
                if (fieldInfo.isAutoIncrement) {
                    if (databaseManager.getDatabaseType().equals("postgresql")) {
                        columnDef.append(" GENERATED BY DEFAULT AS IDENTITY");
                    } else {
                        columnDef.append(" AUTO_INCREMENT");
                    }
                }
                primaryKeyDefinition = columnDef.toString();
            } else {
                columnDefinitions.add(columnDef.toString());
            }
        }
        
        // додати первинний ключ в кінець, якщо він є
        if (primaryKeyDefinition != null) {
            columnDefinitions.add(primaryKeyDefinition);
        }
        
        sql.append(String.join(", ", columnDefinitions));
        sql.append(")");
        
        databaseManager.executeUpdate(sql.toString());
    }
    
    /**
     * отримати тип колонки на основі типу поля
     */
    private String getColumnType(Class<?> fieldType) {
        if (fieldType == String.class) {
            return "VARCHAR(255)";
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return "INTEGER";
        } else if (fieldType == long.class || fieldType == Long.class) {
            return "BIGINT";
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "BOOLEAN";
        } else if (fieldType == double.class || fieldType == Double.class) {
            return "DOUBLE";
        } else if (fieldType == float.class || fieldType == Float.class) {
            return "FLOAT";
        } else if (fieldType == java.util.Date.class || fieldType == java.sql.Date.class) {
            return "DATE";
        } else if (fieldType == java.sql.Timestamp.class) {
            return "TIMESTAMP";
        } else {
            return "VARCHAR(255)"; // тип за замовчуванням
        }
    }
    
    /**
     * отримати кількість сутностей
     * @param clazz клас сутності
     * @return кількість сутностей
     */
    public <T> long count(Class<T> clazz) throws SQLException {
        EntityInfo entityInfo = getEntityInfo(clazz);
        return databaseManager.count(entityInfo.tableName, null);
    }
    
    /**
     * отримати кількість сутностей за умовою
     * @param clazz клас сутності
     * @param whereClause умова WHERE
     * @param parameters параметри
     * @return кількість сутностей
     */
    public <T> long count(Class<T> clazz, String whereClause, Object... parameters) throws SQLException {
        EntityInfo entityInfo = getEntityInfo(clazz);
        return databaseManager.count(entityInfo.tableName, whereClause, parameters);
    }
    
    /**
     * очистити кеш сутностей
     */
    public void clearCache() {
        entityCache.clear();
    }
}