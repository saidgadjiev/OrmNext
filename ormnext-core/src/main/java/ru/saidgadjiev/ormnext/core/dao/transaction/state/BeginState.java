package ru.saidgadjiev.ormnext.core.dao.transaction.state;

import ru.saidgadjiev.ormnext.core.connection.DatabaseConnection;

import java.sql.SQLException;

/**
 * Transaction prepared for begin state.
 *
 * @author Said Gadjiev
 */
public class BeginState implements TransactionState {

    /**
     * Object which has transaction state.
     * @see SessionTransactionContract
     */
    private SessionTransactionContract transaction;

    /**
     * Create a new instance.
     * @param transaction target internal transaction
     */
    public BeginState(SessionTransactionContract transaction) {
        this.transaction = transaction;
    }

    @Override
    public void begin(DatabaseConnection connection) throws SQLException {
        connection.setAutoCommit(false);
        transaction.changeState(new BeginnedState(transaction));
    }

    @Override
    public void commit(DatabaseConnection connection) throws SQLException {
        throw new SQLException("Transaction not beginTransaction");
    }

    @Override
    public void rollback(DatabaseConnection connection) throws SQLException {
        throw new SQLException("Transaction not beginTransaction");
    }
}
