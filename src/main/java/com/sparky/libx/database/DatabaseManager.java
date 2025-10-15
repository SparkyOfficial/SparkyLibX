package com.sparky.libx.database;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * просунутий менеджер баз даних з підтримкою пулу з'єднань, транзакцій і ORM
 * @author Андрій Будильников
 */
public class DatabaseManager {
    
    private final HikariDataSource dataSource;
    private final String databaseType;
    private final AtomicInteger queryCounter = new AtomicInteger(0);
    
    /**
     * створити менеджер бази даних
     * @param jdbcUrl JDBC URL
     * @param username ім'я користувача
     * @param password пароль
     * @param databaseType тип бази даних (mysql, postgresql, sqlite, h2)
     */
    public DatabaseManager(String jdbcUrl, String username, String password, String databaseType) {
        this.databaseType = databaseType.toLowerCase();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // налаштування для різних типів баз даних
        switch (this.databaseType) {
            case "mysql":
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("useLocalSessionState", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");
                config.addDataSourceProperty("cacheResultSetMetadata", "true");
                config.addDataSourceProperty("cacheServerConfiguration", "true");
                config.addDataSourceProperty("elideSetAutoCommits", "true");
                config.addDataSourceProperty("maintainTimeStats", "false");
                break;
            case "postgresql":
                config.addDataSourceProperty("prepareThreshold", "100");
                config.addDataSourceProperty("preparedStatementCacheQueries", "256");
                config.addDataSourceProperty("preparedStatementCacheSizeMiB", "10");
                break;
            case "sqlite":
                config.addDataSourceProperty("journal_mode", "WAL");
                config.addDataSourceProperty("synchronous", "NORMAL");
                break;
            case "h2":
                config.addDataSourceProperty("DB_CLOSE_DELAY", "-1");
                break;
        }
        
        this.dataSource = new HikariDataSource(config);
    }
    
    /**
     * отримати з'єднання з базою даних
     * @return з'єднання
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * виконати SQL запит
     * @param sql SQL запит
     * @param parameters параметри
     * @return кількість змінених рядків
     */
    public int executeUpdate(String sql, Object... parameters) throws SQLException {
        queryCounter.incrementAndGet();
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            setParameters(statement, parameters);
            return statement.executeUpdate();
        }
    }
    
    /**
     * виконати SQL запит з поверненням результату
     * @param sql SQL запит
     * @param parameters параметри
     * @return результат запиту
     */
    public List<Map<String, Object>> executeQuery(String sql, Object... parameters) throws SQLException {
        queryCounter.incrementAndGet();
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            setParameters(statement, parameters);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSetToList(resultSet);
            }
        }
    }
    
    /**
     * виконати SQL запит з поверненням одного результату
     * @param sql SQL запит
     * @param parameters параметри
     * @return результат запиту або null
     */
    public Map<String, Object> executeQuerySingle(String sql, Object... parameters) throws SQLException {
        List<Map<String, Object>> results = executeQuery(sql, parameters);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * виконати пакетний запит
     * @param sql SQL запит
     * @param batchParameters список параметрів для кожного запиту
     * @return масив кількостей змінених рядків
     */
    public int[] executeBatch(String sql, List<Object[]> batchParameters) throws SQLException {
        queryCounter.incrementAndGet();
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (Object[] parameters : batchParameters) {
                setParameters(statement, parameters);
                statement.addBatch();
            }
            
            return statement.executeBatch();
        }
    }
    
    /**
     * встановити параметри для PreparedStatement
     * @param statement PreparedStatement
     * @param parameters параметри
     */
    private void setParameters(PreparedStatement statement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }
    
    /**
     * перетворити ResultSet в список мап
     * @param resultSet ResultSet
     * @return список мап
     */
    private List<Map<String, Object>> resultSetToList(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            results.add(row);
        }
        
        return results;
    }
    
    /**
     * виконати транзакцію
     * @param transaction транзакція
     */
    public void executeTransaction(Transaction transaction) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                transaction.execute(connection);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new SQLException("Транзакція не вдалася", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
    
    /**
     * функціональний інтерфейс для транзакцій
     */
    @FunctionalInterface
    public interface Transaction {
        void execute(Connection connection) throws SQLException;
    }
    
    /**
     * створити таблицю
     * @param tableName назва таблиці
     * @param columns колонки (назва тип)
     */
    public void createTable(String tableName, Map<String, String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName).append(" (");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(entry.getKey()).append(" ").append(entry.getValue());
            first = false;
        }
        sql.append(")");
        
        executeUpdate(sql.toString());
    }
    
    /**
     * вставити дані в таблицю
     * @param tableName назва таблиці
     * @param data дані (колонка значення)
     * @return кількість вставлених рядків
     */
    public int insert(String tableName, Map<String, Object> data) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");
        
        StringBuilder placeholders = new StringBuilder(" VALUES (");
        
        boolean first = true;
        for (String column : data.keySet()) {
            if (!first) {
                sql.append(", ");
                placeholders.append(", ");
            }
            sql.append(column);
            placeholders.append("?");
            first = false;
        }
        
        sql.append(")").append(placeholders).append(")");
        
        return executeUpdate(sql.toString(), data.values().toArray());
    }
    
    /**
     * оновити дані в таблиці
     * @param tableName назва таблиці
     * @param data дані для оновлення (колонка значення)
     * @param whereClause умова WHERE
     * @param whereParameters параметри для WHERE
     * @return кількість оновлених рядків
     */
    public int update(String tableName, Map<String, Object> data, String whereClause, Object... whereParameters) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName).append(" SET ");
        
        boolean first = true;
        for (String column : data.keySet()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(column).append(" = ?");
            first = false;
        }
        
        sql.append(" WHERE ").append(whereClause);
        
        // об'єднати параметри для UPDATE і WHERE
        Object[] allParameters = new Object[data.size() + whereParameters.length];
        int index = 0;
        for (Object value : data.values()) {
            allParameters[index++] = value;
        }
        for (Object param : whereParameters) {
            allParameters[index++] = param;
        }
        
        return executeUpdate(sql.toString(), allParameters);
    }
    
    /**
     * видалити дані з таблиці
     * @param tableName назва таблиці
     * @param whereClause умова WHERE
     * @param whereParameters параметри для WHERE
     * @return кількість видалених рядків
     */
    public int delete(String tableName, String whereClause, Object... whereParameters) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;
        return executeUpdate(sql, whereParameters);
    }
    
    /**
     * вибрати дані з таблиці
     * @param tableName назва таблиці
     * @param columns колонки для вибору (null для всіх)
     * @param whereClause умова WHERE (null якщо немає)
     * @param whereParameters параметри для WHERE
     * @return результат запиту
     */
    public List<Map<String, Object>> select(String tableName, String[] columns, String whereClause, Object... whereParameters) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        if (columns == null || columns.length == 0) {
            sql.append("*");
        } else {
            sql.append(String.join(", ", columns));
        }
        
        sql.append(" FROM ").append(tableName);
        
        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        return executeQuery(sql.toString(), whereParameters);
    }
    
    /**
     * підрахувати кількість рядків в таблиці
     * @param tableName назва таблиці
     * @param whereClause умова WHERE (null якщо немає)
     * @param whereParameters параметри для WHERE
     * @return кількість рядків
     */
    public long count(String tableName, String whereClause, Object... whereParameters) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as count FROM ");
        sql.append(tableName);
        
        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        Map<String, Object> result = executeQuerySingle(sql.toString(), whereParameters);
        return result != null ? ((Number) result.get("count")).longValue() : 0;
    }
    
    /**
     * перевірити чи існує таблиця
     * @param tableName назва таблиці
     * @return true якщо таблиця існує
     */
    public boolean tableExists(String tableName) throws SQLException {
        String sql;
        switch (databaseType) {
            case "mysql":
                sql = "SHOW TABLES LIKE ?";
                break;
            case "postgresql":
                sql = "SELECT tablename FROM pg_tables WHERE tablename = ?";
                break;
            case "sqlite":
            case "h2":
                sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
                break;
            default:
                // спробувати стандартний запит
                sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
                break;
        }
        
        List<Map<String, Object>> results = executeQuery(sql, tableName);
        return !results.isEmpty();
    }
    
    /**
     * отримати кількість виконаних запитів
     * @return кількість запитів
     */
    public int getQueryCount() {
        return queryCounter.get();
    }
    
    /**
     * скинути лічильник запитів
     */
    public void resetQueryCount() {
        queryCounter.set(0);
    }
    
    /**
     * закрити менеджер бази даних
     */
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
    
    /**
     * отримати інформацію про пул з'єднань
     * @return інформація про пул
     */
    public Map<String, Object> getPoolInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("activeConnections", dataSource.getHikariPoolMXBean().getActiveConnections());
        info.put("idleConnections", dataSource.getHikariPoolMXBean().getIdleConnections());
        info.put("totalConnections", dataSource.getHikariPoolMXBean().getTotalConnections());
        info.put("threadsAwaitingConnection", dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        return info;
    }
    
    /**
     * виконати SQL скрипт
     * @param script SQL скрипт
     */
    public void executeScript(String script) throws SQLException {
        String[] statements = script.split(";");
        
        try (Connection connection = getConnection()) {
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    try (PreparedStatement ps = connection.prepareStatement(statement)) {
                        ps.execute();
                    }
                }
            }
        }
    }
    
    /**
     * екранувати назву ідентифікатора
     * @param identifier ідентифікатор
     * @return екранований ідентифікатор
     */
    public String escapeIdentifier(String identifier) {
        switch (databaseType) {
            case "mysql":
                return "`" + identifier + "`";
            case "postgresql":
                return "\"" + identifier + "\"";
            default:
                return identifier;
        }
    }
    
    /**
     * отримати тип бази даних
     * @return тип бази даних
     */
    public String getDatabaseType() {
        return databaseType;
    }
    
    /**
     * отримати URL бази даних
     * @return URL бази даних
     */
    public String getJdbcUrl() {
        return dataSource.getJdbcUrl();
    }
}