package ru.said.miami.orm.core.dao;

import ru.said.miami.orm.core.cache.ObjectCache;
import ru.said.miami.orm.core.cache.ReferenceObjectCache;
import ru.said.miami.orm.core.queryBuilder.PreparedQuery;
import ru.said.miami.orm.core.stamentExecutor.IStatementExecutor;
import ru.said.miami.orm.core.stamentExecutor.StatementValidator;
import ru.said.miami.orm.core.stamentExecutor.object.DataBaseObject;
import ru.said.miami.orm.core.queryBuilder.QueryBuilder;
import ru.said.miami.orm.core.table.TableInfo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Базовый класс для DAO. Используется в DaoBuilder
 * @param <T>
 * @param <ID>
 */
public abstract class BaseDaoImpl<T, ID> implements Dao<T, ID> {

    private final DataSource dataSource;

    private IStatementExecutor<T, ID> statementExecutor;

    private DataBaseObject<T> dataBaseObject;

    protected BaseDaoImpl(DataSource dataSource, TableInfo<T> tableInfo) {
        this.dataSource = dataSource;
        this.dataBaseObject = new DataBaseObject<T>(
                dataSource,
                tableInfo
        );
        this.statementExecutor = new StatementValidator<>(this.dataBaseObject);
    }

    @Override
    public int create(T object) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.create(connection, object);
        }
    }

    @Override
    public boolean createTable(boolean ifNotExist) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.createTable(connection, ifNotExist);
        }
    }

    @Override
    public T queryForId(ID id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.queryForId(connection, id);
        }
    }

    @Override
    public List<T> queryForAll() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.queryForAll(connection);
        }
    }

    @Override
    public int update(T object) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.update(connection, object);
        }
    }

    @Override
    public int delete(T object) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.create(connection, object);
        }
    }

    @Override
    public int deleteById(ID id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.deleteById(connection, id);
        }
    }

    @Override
    public QueryBuilder<T> queryBuilder() {
        return new QueryBuilder<>(dataBaseObject.getTableInfo());
    }

    @Override
    public void caching(boolean flag, ObjectCache objectCache) {
        if (flag) {
            if (objectCache != null) {
                dataBaseObject.setObjectCache(objectCache);
            } else {
                dataBaseObject.setObjectCache(new ReferenceObjectCache());
            }
        } else {
            dataBaseObject.setObjectCache(null);
        }
    }

    @Override
    public boolean dropTable(boolean ifExists) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.dropTable(connection, ifExists);
        }
    }

    @Override
    public void createIndexes() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            statementExecutor.createIndexes(connection);
        }
    }

    @Override
    public void dropIndexes() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            statementExecutor.dropIndexes(connection);
        }
    }

    @Override
    public List<T> query(PreparedQuery preparedQuery) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return statementExecutor.query(preparedQuery, connection);
        }
    }

    public static<T, ID> Dao<T, ID> createDao(DataSource dataSource, TableInfo<T> tableInfoBuilder) {
        return new BaseDaoImpl<T, ID>(dataSource, tableInfoBuilder) {};
    }
}
