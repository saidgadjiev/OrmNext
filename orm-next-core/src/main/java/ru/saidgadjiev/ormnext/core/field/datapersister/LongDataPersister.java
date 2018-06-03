package ru.saidgadjiev.ormnext.core.field.datapersister;

import ru.saidgadjiev.ormnext.core.connectionsource.DatabaseResults;
import ru.saidgadjiev.ormnext.core.field.DataType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Type that persists a long type.
 *
 * @author said gadjiev
 */
public class LongDataPersister extends BaseDataPersister {

    /**
     * Create a new instance.
     */
    public LongDataPersister() {
        super(new Class<?>[] {Long.class, long.class}, Types.BIGINT);
    }

    @Override
    public int getDataType() {
        return DataType.LONG;
    }

    @Override
    public Object readValue(DatabaseResults databaseResults, String columnLabel) throws SQLException {
        return databaseResults.getLong(columnLabel);
    }

    @Override
    protected void setNonNullObject(PreparedStatement preparedStatement, int index, Object value) throws SQLException {
        preparedStatement.setLong(index, (Integer) value);
    }
}