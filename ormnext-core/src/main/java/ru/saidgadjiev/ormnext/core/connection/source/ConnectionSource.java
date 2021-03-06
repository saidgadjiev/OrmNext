package ru.saidgadjiev.ormnext.core.connection.source;

import ru.saidgadjiev.ormnext.core.connection.DatabaseConnection;

import java.sql.SQLException;

/**
 * Connection source.
 *
 * @param <T> connection type
 * @author Said Gadjiev
 */
public interface ConnectionSource<T> extends AutoCloseable {

    /**
     * Retrieve a new connection.
     *
     * @return a new connection
     * @throws SQLException any SQL exceptions
     */
    DatabaseConnection<T> getConnection() throws SQLException;

    /**
     * Release connection.
     *
     * @param connection target connection
     * @throws SQLException any SQL exceptions
     */
    default void releaseConnection(DatabaseConnection<T> connection) throws SQLException {
        connection.close();
    }

    /**
     * Release resources. By default it is an empty.
     *
     * @throws SQLException any SQL exceptions
     */
    default void close() throws SQLException {

    }
}
