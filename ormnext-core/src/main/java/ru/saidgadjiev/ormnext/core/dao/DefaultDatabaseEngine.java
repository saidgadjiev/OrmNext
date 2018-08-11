package ru.saidgadjiev.ormnext.core.dao;

import ru.saidgadjiev.ormnext.core.connection.*;
import ru.saidgadjiev.ormnext.core.dialect.Dialect;
import ru.saidgadjiev.ormnext.core.field.fieldtype.DatabaseColumnType;
import ru.saidgadjiev.ormnext.core.loader.Argument;
import ru.saidgadjiev.ormnext.core.loader.GeneratedKey;
import ru.saidgadjiev.ormnext.core.logger.Log;
import ru.saidgadjiev.ormnext.core.logger.LoggerFactory;
import ru.saidgadjiev.ormnext.core.query.visitor.DefaultVisitor;
import ru.saidgadjiev.ormnext.core.query.visitor.QueryElement;
import ru.saidgadjiev.ormnext.core.query.visitor.element.*;

import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Default database engine.
 *
 * @author Said Gadjiev
 */
public class DefaultDatabaseEngine implements DatabaseEngine<Connection> {

    /**
     * Logger.
     */
    private static final Log LOG = LoggerFactory.getLogger(DefaultDatabaseEngine.class);

    /**
     * Generated key column index.
     */
    private static final int GENERATED_KEY_COLUMN_INDEX = 1;

    /**
     * Database dialect.
     *
     * @see Dialect
     */
    private final Dialect dialect;

    /**
     * Create a new instance.
     *
     * @param dialect target database dialect
     */
    public DefaultDatabaseEngine(Dialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public DatabaseResults select(DatabaseConnection<Connection> databaseConnection,
                                  SelectQuery selectQuery,
                                  Map<Integer, Argument> args) throws SQLException {
        String query = getQuery(selectQuery);

        traceSql(query, args);
        Connection connection = databaseConnection.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        try {
            processArgs(new PreparableObjectImpl(preparedStatement), args);

            ResultSet resultSet = preparedStatement.executeQuery();

            return new DatabaseResultsImpl(resultSet) {
                @Override
                public void close() throws SQLException {
                    resultSet.close();
                    preparedStatement.close();
                }
            };
        } catch (SQLException ex) {
            preparedStatement.close();
            throw ex;
        }
    }

    @Override
    public int create(DatabaseConnection<Connection> databaseConnection,
                      CreateQuery createQuery,
                      Map<Integer, Argument> args,
                      DatabaseColumnType primaryKey,
                      GeneratedKey generatedKey) throws SQLException {
        String query = getQuery(createQuery);

        traceSql(query, args);
        Connection connection = databaseConnection.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            processArgs(new PreparableObjectImpl(statement), args);

            int result = statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

                while (resultSet.next()) {
                    generatedKey.set(readValue(resultSet, resultSetMetaData.getColumnType(GENERATED_KEY_COLUMN_INDEX)));
                }
            }

            return result;
        }
    }

    @Override
    public int create(DatabaseConnection<Connection> databaseConnection,
                      CreateQuery createQuery,
                      List<Map<Integer, Argument>> argList,
                      DatabaseColumnType primaryKey,
                      List<GeneratedKey> generatedKeys) throws SQLException {
        String query = getQuery(createQuery);

        traceSql(query, argList);
        Connection connection = databaseConnection.getConnection();
        Iterator<GeneratedKey> generatedKeyIterator = generatedKeys.iterator();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            PreparableObject preparableObject = new PreparableObjectImpl(statement);
            int result = 0;

            for (Map<Integer, Argument> args : argList) {
                processArgs(preparableObject, args);
                result += statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

                    while (resultSet.next()) {
                        generatedKeyIterator.next().set(readValue(
                                resultSet,
                                resultSetMetaData.getColumnType(GENERATED_KEY_COLUMN_INDEX)
                        ));
                    }
                }
            }

            return result;
        }
    }

    /**
     * Read value by sql type. Use for read generated key.
     *
     * @param resultSet target results
     * @param sqlType   target sql type {@link Types}
     * @return read value
     * @throws SQLException any SQL exceptions
     */
    private Number readValue(ResultSet resultSet, int sqlType) throws SQLException {
        switch (sqlType) {
            case Types.BIGINT:
            case Types.DECIMAL:
            case Types.NUMERIC:
                return resultSet.getLong(GENERATED_KEY_COLUMN_INDEX);
            case Types.INTEGER:
                return resultSet.getInt(GENERATED_KEY_COLUMN_INDEX);
            default:
                throw new SQLException(
                        "Unknown DataType for typeVal " + sqlType + " in column " + GENERATED_KEY_COLUMN_INDEX
                );
        }
    }

    @Override
    public int delete(DatabaseConnection<Connection> databaseConnection,
                      DeleteQuery deleteQuery,
                      Map<Integer, Argument> args) throws SQLException {
        String query = getQuery(deleteQuery);

        traceSql(query, args);
        Connection originialConnection = databaseConnection.getConnection();

        try (PreparedStatement preparedQuery = originialConnection.prepareStatement(query)) {
            processArgs(new PreparableObjectImpl(preparedQuery), args);

            return preparedQuery.executeUpdate();
        }
    }

    @Override
    public int update(DatabaseConnection<Connection> databaseConnection,
                      UpdateQuery updateQuery,
                      Map<Integer, Argument> args) throws SQLException {
        String query = getQuery(updateQuery);

        traceSql(query, args);
        Connection originialConnection = databaseConnection.getConnection();

        try (PreparedStatement preparedQuery = originialConnection.prepareStatement(query)) {
            processArgs(new PreparableObjectImpl(preparedQuery), args);

            return preparedQuery.executeUpdate();
        }
    }

    @Override
    public boolean createTable(DatabaseConnection<Connection> databaseConnection,
                               CreateTableQuery createTableQuery) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        String sql = getQuery(createTableQuery);

        traceSql(sql, null);
        try (Statement statement = connection.createStatement()) {
            statement.execute(getQuery(createTableQuery));

            return true;
        }
    }

    @Override
    public boolean dropTable(DatabaseConnection<Connection> databaseConnection,
                             DropTableQuery dropTableQuery) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        String query = getQuery(dropTableQuery);

        traceSql(query, null);
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);

            return true;
        }
    }

    @Override
    public void createIndex(DatabaseConnection<Connection> databaseConnection,
                            CreateIndexQuery createIndexQuery) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        String sql = getQuery(createIndexQuery);

        traceSql(sql, null);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public void dropIndex(DatabaseConnection<Connection> databaseConnection,
                          DropIndexQuery dropIndexQuery) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        String sql = getQuery(dropIndexQuery);

        traceSql(sql, null);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public DatabaseResults query(DatabaseConnection<Connection> databaseConnection, String query) throws SQLException {
        traceSql(query, null);
        Connection connection = databaseConnection.getConnection();
        Statement statement = connection.createStatement();

        try {
            ResultSet resultSet = statement.executeQuery(query);

            return new DatabaseResultsImpl(resultSet) {
                @Override
                public void close() throws SQLException {
                    resultSet.close();
                    statement.close();
                }
            };
        } catch (SQLException e) {
            statement.close();
            throw e;
        }
    }

    @Override
    public Dialect getDialect() {
        return dialect;
    }

    @Override
    public String prepareQuery(SqlStatement sqlStatement) {
        return getQuery(sqlStatement);
    }

    @Override
    public int executeUpdate(DatabaseConnection<Connection> databaseConnection, String query) throws SQLException {
        Connection connection = databaseConnection.getConnection();

        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(query);
        }
    }

    /**
     * Set args to requested prepared statement.
     *
     * @param preparedStatement target prepared statement
     * @param args              target args
     * @throws SQLException on any SQL problems
     */
    private void processArgs(PreparableObject preparedStatement,
                             Map<Integer, Argument> args) throws SQLException {
        for (Map.Entry<Integer, Argument> entry : args.entrySet()) {
            Argument argument = entry.getValue();
            Object value = argument.getValue();

            argument.getDataPersister().setObject(preparedStatement, entry.getKey(), value);
        }
    }

    /**
     * Make executeQuery by visitor. This engine use {@link DefaultVisitor}.
     *
     * @param queryElement target visitor element
     * @return sql executeQuery
     */
    private String getQuery(QueryElement queryElement) {
        DefaultVisitor defaultVisitor = new DefaultVisitor(dialect);

        queryElement.accept(defaultVisitor);

        return defaultVisitor.getQuery();
    }

    /**
     * Trace sql.
     *
     * @param query target executeQuery
     * @param args target args
     */
    private void traceSql(String query, Object args) {
        if (args == null) {
            LOG.debug("SQL(" + query + ")");
        } else {
            LOG.debug("SQL(" + query + ") args(" + args + ")");
        }
    }
}
