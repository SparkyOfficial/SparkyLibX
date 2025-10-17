package com.sparky.libx.db;

import com.sparky.libx.math.Vector3D;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import javax.sql.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.zip.*;

/**
 * Advanced Database Management Framework for Minecraft Plugins
 * Provides capabilities for database management, ORM, connection pooling, and advanced querying
 * 
 * @author Андрій Будильников
 */
public class AdvancedDatabaseManagement {
    
    /**
     * Represents a database connection pool
     */
    public static class ConnectionPool {
        private final String url;
        private final String username;
        private final String password;
        private final int maxSize;
        private final BlockingQueue<PooledConnection> availableConnections;
        private final List<PooledConnection> usedConnections;
        private final Lock poolLock;
        private volatile boolean closed;
        
        public ConnectionPool(String url, String username, String password, int maxSize) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.maxSize = maxSize;
            this.availableConnections = new LinkedBlockingQueue<>();
            this.usedConnections = new ArrayList<>();
            this.poolLock = new ReentrantLock();
            this.closed = false;
            
            // Initialize pool with minimum connections
            initializePool(Math.min(5, maxSize));
        }
        
        /**
         * Initializes the connection pool
         */
        private void initializePool(int count) {
            for (int i = 0; i < count; i++) {
                try {
                    Connection connection = DriverManager.getConnection(url, username, password);
                    PooledConnection pooledConnection = new PooledConnection(connection, this);
                    availableConnections.offer(pooledConnection);
                } catch (SQLException e) {
                    System.err.println("Failed to create connection: " + e.getMessage());
                }
            }
        }
        
        /**
         * Gets a connection from the pool
         */
        public Connection getConnection() throws SQLException {
            if (closed) {
                throw new SQLException("Connection pool is closed");
            }
            
            PooledConnection pooledConnection = availableConnections.poll();
            
            if (pooledConnection == null && usedConnections.size() < maxSize) {
                // Create new connection if pool is not full
                Connection connection = DriverManager.getConnection(url, username, password);
                pooledConnection = new PooledConnection(connection, this);
            }
            
            if (pooledConnection == null) {
                // Wait for available connection
                try {
                    pooledConnection = availableConnections.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for connection", e);
                }
            }
            
            poolLock.lock();
            try {
                usedConnections.add(pooledConnection);
            } finally {
                poolLock.unlock();
            }
            
            return pooledConnection;
        }
        
        /**
         * Returns a connection to the pool
         */
        void returnConnection(PooledConnection pooledConnection) {
            poolLock.lock();
            try {
                usedConnections.remove(pooledConnection);
                if (!closed && pooledConnection.isValid()) {
                    availableConnections.offer(pooledConnection);
                } else {
                    // Close invalid connection
                    try {
                        pooledConnection.getRealConnection().close();
                    } catch (SQLException e) {
                        System.err.println("Error closing connection: " + e.getMessage());
                    }
                }
            } finally {
                poolLock.unlock();
            }
        }
        
        /**
         * Closes the connection pool
         */
        public void close() {
            if (closed) {
                return;
            }
            
            closed = true;
            
            // Close all connections
            poolLock.lock();
            try {
                for (PooledConnection pooledConnection : availableConnections) {
                    try {
                        pooledConnection.getRealConnection().close();
                    } catch (SQLException e) {
                        System.err.println("Error closing connection: " + e.getMessage());
                    }
                }
                
                for (PooledConnection pooledConnection : usedConnections) {
                    try {
                        pooledConnection.getRealConnection().close();
                    } catch (SQLException e) {
                        System.err.println("Error closing connection: " + e.getMessage());
                    }
                }
                
                availableConnections.clear();
                usedConnections.clear();
            } finally {
                poolLock.unlock();
            }
        }
        
        /**
         * Gets the number of available connections
         */
        public int getAvailableConnectionCount() {
            return availableConnections.size();
        }
        
        /**
         * Gets the number of used connections
         */
        public int getUsedConnectionCount() {
            poolLock.lock();
            try {
                return usedConnections.size();
            } finally {
                poolLock.unlock();
            }
        }
    }
    
    /**
     * Represents a pooled database connection
     */
    public static class PooledConnection implements Connection {
        private final Connection realConnection;
        private final ConnectionPool pool;
        private volatile boolean closed;
        
        public PooledConnection(Connection realConnection, ConnectionPool pool) {
            this.realConnection = realConnection;
            this.pool = pool;
            this.closed = false;
        }
        
        /**
         * Gets the real database connection
         */
        public Connection getRealConnection() {
            return realConnection;
        }
        
        /**
         * Checks if the connection is valid
         */
        public boolean isValid() {
            try {
                return !closed && realConnection != null && !realConnection.isClosed();
            } catch (SQLException e) {
                return false;
            }
        }
        
        @Override
        public Statement createStatement() throws SQLException {
            checkClosed();
            return realConnection.createStatement();
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            checkClosed();
            return realConnection.prepareStatement(sql);
        }
        
        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            checkClosed();
            return realConnection.prepareCall(sql);
        }
        
        @Override
        public String nativeSQL(String sql) throws SQLException {
            checkClosed();
            return realConnection.nativeSQL(sql);
        }
        
        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            checkClosed();
            realConnection.setAutoCommit(autoCommit);
        }
        
        @Override
        public boolean getAutoCommit() throws SQLException {
            checkClosed();
            return realConnection.getAutoCommit();
        }
        
        @Override
        public void commit() throws SQLException {
            checkClosed();
            realConnection.commit();
        }
        
        @Override
        public void rollback() throws SQLException {
            checkClosed();
            realConnection.rollback();
        }
        
        @Override
        public void close() throws SQLException {
            if (!closed) {
                closed = true;
                pool.returnConnection(this);
            }
        }
        
        @Override
        public boolean isClosed() throws SQLException {
            return closed || realConnection.isClosed();
        }
        
        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            checkClosed();
            return realConnection.getMetaData();
        }
        
        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            checkClosed();
            realConnection.setReadOnly(readOnly);
        }
        
        @Override
        public boolean isReadOnly() throws SQLException {
            checkClosed();
            return realConnection.isReadOnly();
        }
        
        @Override
        public void setCatalog(String catalog) throws SQLException {
            checkClosed();
            realConnection.setCatalog(catalog);
        }
        
        @Override
        public String getCatalog() throws SQLException {
            checkClosed();
            return realConnection.getCatalog();
        }
        
        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            checkClosed();
            realConnection.setTransactionIsolation(level);
        }
        
        @Override
        public int getTransactionIsolation() throws SQLException {
            checkClosed();
            return realConnection.getTransactionIsolation();
        }
        
        @Override
        public SQLWarning getWarnings() throws SQLException {
            checkClosed();
            return realConnection.getWarnings();
        }
        
        @Override
        public void clearWarnings() throws SQLException {
            checkClosed();
            realConnection.clearWarnings();
        }
        
        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            checkClosed();
            return realConnection.createStatement(resultSetType, resultSetConcurrency);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            checkClosed();
            return realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }
        
        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            checkClosed();
            return realConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }
        
        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            checkClosed();
            return realConnection.getTypeMap();
        }
        
        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            checkClosed();
            realConnection.setTypeMap(map);
        }
        
        @Override
        public void setHoldability(int holdability) throws SQLException {
            checkClosed();
            realConnection.setHoldability(holdability);
        }
        
        @Override
        public int getHoldability() throws SQLException {
            checkClosed();
            return realConnection.getHoldability();
        }
        
        @Override
        public Savepoint setSavepoint() throws SQLException {
            checkClosed();
            return realConnection.setSavepoint();
        }
        
        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            checkClosed();
            return realConnection.setSavepoint(name);
        }
        
        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            checkClosed();
            realConnection.rollback(savepoint);
        }
        
        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            checkClosed();
            realConnection.releaseSavepoint(savepoint);
        }
        
        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkClosed();
            return realConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkClosed();
            return realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkClosed();
            return realConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            checkClosed();
            return realConnection.prepareStatement(sql, autoGeneratedKeys);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            checkClosed();
            return realConnection.prepareStatement(sql, columnIndexes);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            checkClosed();
            return realConnection.prepareStatement(sql, columnNames);
        }
        
        @Override
        public Clob createClob() throws SQLException {
            checkClosed();
            return realConnection.createClob();
        }
        
        @Override
        public Blob createBlob() throws SQLException {
            checkClosed();
            return realConnection.createBlob();
        }
        
        @Override
        public NClob createNClob() throws SQLException {
            checkClosed();
            return realConnection.createNClob();
        }
        
        @Override
        public SQLXML createSQLXML() throws SQLException {
            checkClosed();
            return realConnection.createSQLXML();
        }
        
        @Override
        public boolean isValid(int timeout) throws SQLException {
            return !closed && realConnection.isValid(timeout);
        }
        
        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            try {
                checkClosed();
                realConnection.setClientInfo(name, value);
            } catch (SQLException e) {
                throw new SQLClientInfoException(e.getMessage(), e.getSQLState(), e.getErrorCode(), null, e);
            }
        }
        
        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            try {
                checkClosed();
                realConnection.setClientInfo(properties);
            } catch (SQLException e) {
                throw new SQLClientInfoException(e.getMessage(), e.getSQLState(), e.getErrorCode(), null, e);
            }
        }
        
        @Override
        public String getClientInfo(String name) throws SQLException {
            checkClosed();
            return realConnection.getClientInfo(name);
        }
        
        @Override
        public Properties getClientInfo() throws SQLException {
            checkClosed();
            return realConnection.getClientInfo();
        }
        
        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            checkClosed();
            return realConnection.createArrayOf(typeName, elements);
        }
        
        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            checkClosed();
            return realConnection.createStruct(typeName, attributes);
        }
        
        @Override
        public void setSchema(String schema) throws SQLException {
            checkClosed();
            realConnection.setSchema(schema);
        }
        
        @Override
        public String getSchema() throws SQLException {
            checkClosed();
            return realConnection.getSchema();
        }
        
        @Override
        public void abort(Executor executor) throws SQLException {
            checkClosed();
            realConnection.abort(executor);
        }
        
        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            checkClosed();
            realConnection.setNetworkTimeout(executor, milliseconds);
        }
        
        @Override
        public int getNetworkTimeout() throws SQLException {
            checkClosed();
            return realConnection.getNetworkTimeout();
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            checkClosed();
            return realConnection.unwrap(iface);
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            checkClosed();
            return realConnection.isWrapperFor(iface);
        }
        
        /**
         * Checks if the connection is closed and throws an exception if it is
         */
        private void checkClosed() throws SQLException {
            if (closed) {
                throw new SQLException("Connection is closed");
            }
        }
    }
    
    /**
     * Represents an object-relational mapping (ORM) framework
     */
    public static class ORMFramework {
        private final ConnectionPool connectionPool;
        private final Map<Class<?>, EntityMetadata> entityMetadataCache;
        private final Lock cacheLock;
        
        public ORMFramework(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            this.entityMetadataCache = new ConcurrentHashMap<>();
            this.cacheLock = new ReentrantLock();
        }
        
        /**
         * Saves an entity to the database
         */
        public <T> void save(T entity) throws SQLException {
            Class<?> entityClass = entity.getClass();
            EntityMetadata metadata = getEntityMetadata(entityClass);
            
            Connection connection = connectionPool.getConnection();
            try {
                if (metadata.getIdValue(entity) == null) {
                    // Insert new entity
                    insertEntity(connection, entity, metadata);
                } else {
                    // Update existing entity
                    updateEntity(connection, entity, metadata);
                }
            } finally {
                connection.close();
            }
        }
        
        /**
         * Finds an entity by ID
         */
        public <T> T findById(Class<T> entityClass, Object id) throws SQLException {
            EntityMetadata metadata = getEntityMetadata(entityClass);
            
            Connection connection = connectionPool.getConnection();
            try {
                return findEntityById(connection, entityClass, id, metadata);
            } finally {
                connection.close();
            }
        }
        
        /**
         * Finds all entities of a given type
         */
        public <T> List<T> findAll(Class<T> entityClass) throws SQLException {
            EntityMetadata metadata = getEntityMetadata(entityClass);
            
            Connection connection = connectionPool.getConnection();
            try {
                return findAllEntities(connection, entityClass, metadata);
            } finally {
                connection.close();
            }
        }
        
        /**
         * Deletes an entity from the database
         */
        public <T> void delete(T entity) throws SQLException {
            Class<?> entityClass = entity.getClass();
            EntityMetadata metadata = getEntityMetadata(entityClass);
            
            Connection connection = connectionPool.getConnection();
            try {
                deleteEntity(connection, entity, metadata);
            } finally {
                connection.close();
            }
        }
        
        /**
         * Gets entity metadata, creating it if necessary
         */
        private EntityMetadata getEntityMetadata(Class<?> entityClass) throws SQLException {
            EntityMetadata metadata = entityMetadataCache.get(entityClass);
            if (metadata == null) {
                cacheLock.lock();
                try {
                    metadata = entityMetadataCache.get(entityClass);
                    if (metadata == null) {
                        metadata = new EntityMetadata(entityClass);
                        entityMetadataCache.put(entityClass, metadata);
                    }
                } finally {
                    cacheLock.unlock();
                }
            }
            return metadata;
        }
        
        /**
         * Inserts a new entity into the database
         */
        private <T> void insertEntity(Connection connection, T entity, EntityMetadata metadata) throws SQLException {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(metadata.getTableName()).append(" (");
            
            List<String> columnNames = new ArrayList<>();
            List<String> placeholders = new ArrayList<>();
            
            for (FieldMetadata field : metadata.getFields()) {
                if (!field.isId() || metadata.getIdValue(entity) != null) {
                    columnNames.add(field.getColumnName());
                    placeholders.add("?");
                }
            }
            
            sql.append(String.join(", ", columnNames));
            sql.append(") VALUES (");
            sql.append(String.join(", ", placeholders));
            sql.append(")");
            
            PreparedStatement statement = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            try {
                int index = 1;
                for (FieldMetadata field : metadata.getFields()) {
                    if (!field.isId() || metadata.getIdValue(entity) != null) {
                        try {
                            statement.setObject(index++, field.getValue(entity));
                        } catch (Exception e) {
                            throw new SQLException("Failed to get value for field: " + field.getFieldName(), e);
                        }
                    }
                }
                
                statement.executeUpdate();
                
                // Get generated ID if needed
                if (metadata.getIdField() != null && metadata.getIdValue(entity) == null) {
                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        Object id = generatedKeys.getObject(1);
                        try {
                            metadata.getIdField().setValue(entity, id);
                        } catch (Exception e) {
                            throw new SQLException("Failed to set ID value", e);
                        }
                    }
                    generatedKeys.close();
                }
            } finally {
                statement.close();
            }
        }
        
        /**
         * Updates an existing entity in the database
         */
        private <T> void updateEntity(Connection connection, T entity, EntityMetadata metadata) throws SQLException {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(metadata.getTableName()).append(" SET ");
            
            List<String> setClauses = new ArrayList<>();
            for (FieldMetadata field : metadata.getFields()) {
                if (!field.isId()) {
                    setClauses.add(field.getColumnName() + " = ?");
                }
            }
            
            sql.append(String.join(", ", setClauses));
            sql.append(" WHERE ").append(metadata.getIdField().getColumnName()).append(" = ?");
            
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            try {
                int index = 1;
                for (FieldMetadata field : metadata.getFields()) {
                    if (!field.isId()) {
                        try {
                            statement.setObject(index++, field.getValue(entity));
                        } catch (Exception e) {
                            throw new SQLException("Failed to get value for field: " + field.getFieldName(), e);
                        }
                    }
                }
                
                statement.setObject(index, metadata.getIdValue(entity));
                statement.executeUpdate();
            } finally {
                statement.close();
            }
        }
        
        /**
         * Finds an entity by ID
         */
        private <T> T findEntityById(Connection connection, Class<T> entityClass, Object id, EntityMetadata metadata) throws SQLException {
            String sql = "SELECT * FROM " + metadata.getTableName() + " WHERE " + metadata.getIdField().getColumnName() + " = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                statement.setObject(1, id);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    return createEntityFromResultSet(resultSet, entityClass, metadata);
                }
                
                return null;
            } finally {
                statement.close();
            }
        }
        
        /**
         * Finds all entities of a given type
         */
        private <T> List<T> findAllEntities(Connection connection, Class<T> entityClass, EntityMetadata metadata) throws SQLException {
            String sql = "SELECT * FROM " + metadata.getTableName();
            
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                ResultSet resultSet = statement.executeQuery();
                List<T> entities = new ArrayList<>();
                
                while (resultSet.next()) {
                    entities.add(createEntityFromResultSet(resultSet, entityClass, metadata));
                }
                
                return entities;
            } finally {
                statement.close();
            }
        }
        
        /**
         * Deletes an entity from the database
         */
        private <T> void deleteEntity(Connection connection, T entity, EntityMetadata metadata) throws SQLException {
            String sql = "DELETE FROM " + metadata.getTableName() + " WHERE " + metadata.getIdField().getColumnName() + " = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                statement.setObject(1, metadata.getIdValue(entity));
                statement.executeUpdate();
            } finally {
                statement.close();
            }
        }
        
        /**
         * Creates an entity from a ResultSet
         */
        private <T> T createEntityFromResultSet(ResultSet resultSet, Class<T> entityClass, EntityMetadata metadata) throws SQLException {
            try {
                T entity = entityClass.newInstance();
                
                for (FieldMetadata field : metadata.getFields()) {
                    Object value = resultSet.getObject(field.getColumnName());
                    try {
                        field.setValue(entity, value);
                    } catch (Exception e) {
                        throw new SQLException("Failed to set value for field: " + field.getFieldName(), e);
                    }
                }
                
                return entity;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new SQLException("Failed to create entity instance", e);
            }
        }
    }
    
    /**
     * Represents metadata for an entity class
     */
    public static class EntityMetadata {
        private final String tableName;
        private final List<FieldMetadata> fields;
        private final FieldMetadata idField;
        
        public EntityMetadata(Class<?> entityClass) throws SQLException {
            this.tableName = entityClass.getSimpleName().toLowerCase();
            this.fields = new ArrayList<>();
            FieldMetadata idFieldTemp = null;
            
            try {
                // Use reflection to analyze the class
                java.lang.reflect.Field[] declaredFields = entityClass.getDeclaredFields();
                for (java.lang.reflect.Field field : declaredFields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    String columnName = fieldName; // Could be customized with annotations
                    
                    // Check if this is the ID field (simplified check)
                    boolean isId = fieldName.equals("id") || fieldName.endsWith("Id");
                    
                    FieldMetadata fieldMetadata = new FieldMetadata(fieldName, columnName, isId);
                    this.fields.add(fieldMetadata);
                    
                    if (isId) {
                        idFieldTemp = fieldMetadata;
                    }
                }
                
                this.idField = idFieldTemp;
            } catch (Exception e) {
                throw new SQLException("Failed to create entity metadata", e);
            }
        }
        
        public String getTableName() {
            return tableName;
        }
        
        public List<FieldMetadata> getFields() {
            return new ArrayList<>(fields);
        }
        
        public FieldMetadata getIdField() {
            return idField;
        }
        
        public Object getIdValue(Object entity) {
            // Simplified implementation
            return null;
        }
    }
    
    /**
     * Represents metadata for an entity field
     */
    public static class FieldMetadata {
        private final String fieldName;
        private final String columnName;
        private final boolean isId;
        
        public FieldMetadata(String fieldName, String columnName, boolean isId) {
            this.fieldName = fieldName;
            this.columnName = columnName;
            this.isId = isId;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        public boolean isId() {
            return isId;
        }
        
        public Object getValue(Object entity) throws Exception {
            java.lang.reflect.Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        }
        
        public void setValue(Object entity, Object value) throws Exception {
            java.lang.reflect.Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        }
    }
    
    /**
     * Represents a database migration utility
     */
    public static class DatabaseMigration {
        private final ConnectionPool connectionPool;
        private final List<Migration> migrations;
        private final Lock migrationLock;
        
        public DatabaseMigration(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            this.migrations = new ArrayList<>();
            this.migrationLock = new ReentrantLock();
        }
        
        /**
         * Adds a migration to the migration list
         */
        public void addMigration(Migration migration) {
            migrationLock.lock();
            try {
                migrations.add(migration);
                Collections.sort(migrations);
            } finally {
                migrationLock.unlock();
            }
        }
        
        /**
         * Runs all pending migrations
         */
        public void migrate() throws SQLException {
            // Create migrations table if it doesn't exist
            createMigrationsTable();
            
            // Get list of applied migrations
            Set<String> appliedMigrations = getAppliedMigrations();
            
            // Apply pending migrations
            migrationLock.lock();
            try {
                for (Migration migration : migrations) {
                    if (!appliedMigrations.contains(migration.getId())) {
                        applyMigration(migration);
                        recordMigration(migration.getId());
                    }
                }
            } finally {
                migrationLock.unlock();
            }
        }
        
        /**
         * Creates the migrations table
         */
        private void createMigrationsTable() throws SQLException {
            Connection connection = connectionPool.getConnection();
            try {
                Statement statement = connection.createStatement();
                try {
                    statement.execute("CREATE TABLE IF NOT EXISTS schema_migrations (" +
                                    "id VARCHAR(255) PRIMARY KEY, " +
                                    "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        }
        
        /**
         * Gets the list of applied migrations
         */
        private Set<String> getAppliedMigrations() throws SQLException {
            Set<String> appliedMigrations = new HashSet<>();
            
            Connection connection = connectionPool.getConnection();
            try {
                Statement statement = connection.createStatement();
                try {
                    ResultSet resultSet = statement.executeQuery("SELECT id FROM schema_migrations");
                    while (resultSet.next()) {
                        appliedMigrations.add(resultSet.getString("id"));
                    }
                    resultSet.close();
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
            
            return appliedMigrations;
        }
        
        /**
         * Applies a migration
         */
        private void applyMigration(Migration migration) throws SQLException {
            System.out.println("Applying migration: " + migration.getId());
            
            Connection connection = connectionPool.getConnection();
            try {
                connection.setAutoCommit(false);
                
                try {
                    migration.apply(connection);
                    connection.commit();
                    System.out.println("Migration " + migration.getId() + " applied successfully");
                } catch (SQLException e) {
                    connection.rollback();
                    System.err.println("Failed to apply migration " + migration.getId() + ": " + e.getMessage());
                    throw e;
                }
            } finally {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
        
        /**
         * Records a migration as applied
         */
        private void recordMigration(String migrationId) throws SQLException {
            Connection connection = connectionPool.getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO schema_migrations (id) VALUES (?)");
                try {
                    statement.setString(1, migrationId);
                    statement.executeUpdate();
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        }
    }
    
    /**
     * Represents a database migration
     */
    public static class Migration implements Comparable<Migration> {
        private final String id;
        private final int version;
        private final MigrationFunction migrationFunction;
        
        public Migration(String id, int version, MigrationFunction migrationFunction) {
            this.id = id;
            this.version = version;
            this.migrationFunction = migrationFunction;
        }
        
        public String getId() {
            return id;
        }
        
        public int getVersion() {
            return version;
        }
        
        public void apply(Connection connection) throws SQLException {
            migrationFunction.apply(connection);
        }
        
        @Override
        public int compareTo(Migration other) {
            return Integer.compare(this.version, other.version);
        }
    }
    
    /**
     * Functional interface for migration functions
     */
    @FunctionalInterface
    public interface MigrationFunction {
        void apply(Connection connection) throws SQLException;
    }
    
    /**
     * Represents a database query builder
     */
    public static class QueryBuilder {
        private final ConnectionPool connectionPool;
        private final StringBuilder query;
        private final List<Object> parameters;
        
        public QueryBuilder(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            this.query = new StringBuilder();
            this.parameters = new ArrayList<>();
        }
        
        /**
         * Starts a SELECT query
         */
        public QueryBuilder select(String... columns) {
            query.append("SELECT ");
            if (columns.length == 0) {
                query.append("*");
            } else {
                query.append(String.join(", ", columns));
            }
            return this;
        }
        
        /**
         * Adds a FROM clause
         */
        public QueryBuilder from(String table) {
            query.append(" FROM ").append(table);
            return this;
        }
        
        /**
         * Adds a WHERE clause
         */
        public QueryBuilder where(String condition) {
            query.append(" WHERE ").append(condition);
            return this;
        }
        
        /**
         * Adds an AND condition
         */
        public QueryBuilder and(String condition) {
            query.append(" AND ").append(condition);
            return this;
        }
        
        /**
         * Adds an OR condition
         */
        public QueryBuilder or(String condition) {
            query.append(" OR ").append(condition);
            return this;
        }
        
        /**
         * Adds a parameter to the query
         */
        public QueryBuilder param(Object value) {
            parameters.add(value);
            return this;
        }
        
        /**
         * Adds an ORDER BY clause
         */
        public QueryBuilder orderBy(String... columns) {
            query.append(" ORDER BY ").append(String.join(", ", columns));
            return this;
        }
        
        /**
         * Adds a LIMIT clause
         */
        public QueryBuilder limit(int limit) {
            query.append(" LIMIT ").append(limit);
            return this;
        }
        
        /**
         * Adds an OFFSET clause
         */
        public QueryBuilder offset(int offset) {
            query.append(" OFFSET ").append(offset);
            return this;
        }
        
        /**
         * Executes the query and returns the results
         */
        public List<Map<String, Object>> executeQuery() throws SQLException {
            Connection connection = connectionPool.getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(query.toString());
                try {
                    // Set parameters
                    for (int i = 0; i < parameters.size(); i++) {
                        statement.setObject(i + 1, parameters.get(i));
                    }
                    
                    ResultSet resultSet = statement.executeQuery();
                    List<Map<String, Object>> results = new ArrayList<>();
                    
                    // Get column metadata
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    String[] columnNames = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        columnNames[i] = metaData.getColumnName(i + 1);
                    }
                    
                    // Process results
                    while (resultSet.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 0; i < columnCount; i++) {
                            row.put(columnNames[i], resultSet.getObject(i + 1));
                        }
                        results.add(row);
                    }
                    
                    resultSet.close();
                    return results;
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        }
        
        /**
         * Executes the query and returns the number of affected rows
         */
        public int executeUpdate() throws SQLException {
            Connection connection = connectionPool.getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(query.toString());
                try {
                    // Set parameters
                    for (int i = 0; i < parameters.size(); i++) {
                        statement.setObject(i + 1, parameters.get(i));
                    }
                    
                    return statement.executeUpdate();
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        }
        
        /**
         * Gets the built query string
         */
        public String getQuery() {
            return query.toString();
        }
        
        /**
         * Gets the parameters
         */
        public List<Object> getParameters() {
            return new ArrayList<>(parameters);
        }
    }
    
    /**
     * Represents a database transaction manager
     */
    public static class TransactionManager {
        private final ConnectionPool connectionPool;
        private final ThreadLocal<TransactionContext> transactionContext;
        
        public TransactionManager(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            this.transactionContext = new ThreadLocal<>();
        }
        
        /**
         * Begins a new transaction
         */
        public void beginTransaction() throws SQLException {
            if (transactionContext.get() != null) {
                throw new SQLException("Transaction already in progress");
            }
            
            Connection connection = connectionPool.getConnection();
            connection.setAutoCommit(false);
            
            TransactionContext context = new TransactionContext(connection);
            transactionContext.set(context);
        }
        
        /**
         * Commits the current transaction
         */
        public void commit() throws SQLException {
            TransactionContext context = transactionContext.get();
            if (context == null) {
                throw new SQLException("No transaction in progress");
            }
            
            try {
                context.getConnection().commit();
            } finally {
                cleanupTransaction(context);
            }
        }
        
        /**
         * Rolls back the current transaction
         */
        public void rollback() throws SQLException {
            TransactionContext context = transactionContext.get();
            if (context == null) {
                throw new SQLException("No transaction in progress");
            }
            
            try {
                context.getConnection().rollback();
            } finally {
                cleanupTransaction(context);
            }
        }
        
        /**
         * Cleans up a transaction context
         */
        private void cleanupTransaction(TransactionContext context) {
            try {
                context.getConnection().setAutoCommit(true);
                context.getConnection().close();
            } catch (SQLException e) {
                System.err.println("Error cleaning up transaction: " + e.getMessage());
            } finally {
                transactionContext.remove();
            }
        }
        
        /**
         * Gets the current transaction connection
         */
        public Connection getCurrentConnection() throws SQLException {
            TransactionContext context = transactionContext.get();
            if (context == null) {
                throw new SQLException("No transaction in progress");
            }
            return context.getConnection();
        }
        
        /**
         * Executes a transactional operation
         */
        public <T> T executeInTransaction(TransactionCallback<T> callback) throws SQLException {
            boolean isNewTransaction = (transactionContext.get() == null);
            
            if (isNewTransaction) {
                beginTransaction();
            }
            
            try {
                T result = callback.execute(getCurrentConnection());
                
                if (isNewTransaction) {
                    commit();
                }
                
                return result;
            } catch (Exception e) {
                if (isNewTransaction) {
                    rollback();
                }
                throw new SQLException("Transaction failed", e);
            }
        }
    }
    
    /**
     * Represents a transaction context
     */
    private static class TransactionContext {
        private final Connection connection;
        
        public TransactionContext(Connection connection) {
            this.connection = connection;
        }
        
        public Connection getConnection() {
            return connection;
        }
    }
    
    /**
     * Functional interface for transaction callbacks
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection connection) throws Exception;
    }
    
    /**
     * Represents a database monitoring utility
     */
    public static class DatabaseMonitor {
        private final ConnectionPool connectionPool;
        private final ScheduledExecutorService scheduler;
        private final Map<String, QueryStats> queryStats;
        private final Lock statsLock;
        
        private static class QueryStats {
            volatile long executionCount;
            volatile long totalTime;
            volatile long maxTime;
            volatile long errorCount;
            
            QueryStats() {
                this.executionCount = 0;
                this.totalTime = 0;
                this.maxTime = 0;
                this.errorCount = 0;
            }
        }
        
        public DatabaseMonitor(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            this.scheduler = Executors.newScheduledThreadPool(1);
            this.queryStats = new ConcurrentHashMap<>();
            this.statsLock = new ReentrantLock();
            
            // Start monitoring
            scheduler.scheduleWithFixedDelay(this::printStats, 30, 30, TimeUnit.SECONDS);
        }
        
        /**
         * Records query execution statistics
         */
        public void recordQuery(String query, long executionTime, boolean success) {
            QueryStats stats = queryStats.computeIfAbsent(query, k -> new QueryStats());
            
            stats.executionCount++;
            stats.totalTime += executionTime;
            
            if (executionTime > stats.maxTime) {
                stats.maxTime = executionTime;
            }
            
            if (!success) {
                stats.errorCount++;
            }
        }
        
        /**
         * Gets query statistics
         */
        public QueryStatistics getQueryStatistics(String query) {
            QueryStats stats = queryStats.get(query);
            if (stats == null) {
                return new QueryStatistics(0, 0, 0, 0, 0);
            }
            
            long executionCount = stats.executionCount;
            double averageTime = executionCount > 0 ? (double) stats.totalTime / executionCount : 0;
            double errorRate = executionCount > 0 ? (double) stats.errorCount / executionCount : 0;
            
            return new QueryStatistics(executionCount, stats.totalTime, averageTime, stats.maxTime, errorRate);
        }
        
        /**
         * Prints database statistics
         */
        private void printStats() {
            System.out.println("=== Database Monitor Statistics ===");
            System.out.println("Connection Pool - Available: " + connectionPool.getAvailableConnectionCount() + 
                             ", Used: " + connectionPool.getUsedConnectionCount());
            
            for (Map.Entry<String, QueryStats> entry : queryStats.entrySet()) {
                String query = entry.getKey();
                QueryStatistics stats = getQueryStatistics(query);
                System.out.printf("Query: %s%n", query.substring(0, Math.min(50, query.length())) + (query.length() > 50 ? "..." : ""));
                System.out.printf("  Executions: %d, Avg Time: %.2fms, Max Time: %dms, Error Rate: %.2f%%%n",
                    stats.getExecutionCount(), stats.getAverageTime(), stats.getMaxTime(), stats.getErrorRate() * 100);
            }
            System.out.println("==================================");
        }
        
        /**
         * Shuts down the monitor
         */
        public void shutdown() {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Represents query statistics
     */
    public static class QueryStatistics {
        private final long executionCount;
        private final long totalTime;
        private final double averageTime;
        private final long maxTime;
        private final double errorRate;
        
        public QueryStatistics(long executionCount, long totalTime, double averageTime, long maxTime, double errorRate) {
            this.executionCount = executionCount;
            this.totalTime = totalTime;
            this.averageTime = averageTime;
            this.maxTime = maxTime;
            this.errorRate = errorRate;
        }
        
        public long getExecutionCount() {
            return executionCount;
        }
        
        public long getTotalTime() {
            return totalTime;
        }
        
        public double getAverageTime() {
            return averageTime;
        }
        
        public long getMaxTime() {
            return maxTime;
        }
        
        public double getErrorRate() {
            return errorRate;
        }
    }
}