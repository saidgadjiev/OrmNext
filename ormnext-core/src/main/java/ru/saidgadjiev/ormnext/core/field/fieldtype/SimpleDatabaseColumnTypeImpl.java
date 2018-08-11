package ru.saidgadjiev.ormnext.core.field.fieldtype;

import ru.saidgadjiev.ormnext.core.exception.DefaultConstructorNotFoundException;
import ru.saidgadjiev.ormnext.core.exception.InstantiationException;
import ru.saidgadjiev.ormnext.core.field.*;
import ru.saidgadjiev.ormnext.core.field.datapersister.ColumnConverter;
import ru.saidgadjiev.ormnext.core.field.datapersister.DataPersister;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.DatabaseEntityMetadata;
import ru.saidgadjiev.ormnext.core.table.internal.visitor.EntityMetadataVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class represent simple database column annotated by {@link DatabaseColumn}.
 *
 * @author Said Gadjiev
 */
public final class SimpleDatabaseColumnTypeImpl extends BaseDatabaseColumnType {

    /**
     * Column name.
     */
    private String columnName;

    /**
     * Column field.
     */
    private Field field;

    /**
     * Column length.
     */
    private int length;

    /**
     * Column data persister.
     * @see DataPersister
     */
    private DataPersister dataPersister;

    /**
     * Helper for field access.
     */
    private FieldAccessor fieldAccessor;

    /**
     * Not null.
     */
    private boolean notNull;

    /**
     * Is id.
     */
    private boolean id;

    /**
     * Is generated.
     */
    private boolean generated;

    /**
     * Default definition.
     */
    private String defaultDefinition;

    /**
     * This column owner table name.
     */
    private String tableName;

    /**
     * Is insertable.
     */
    private boolean insertable;

    /**
     * Is updatable.
     */
    private boolean updatable;

    /**
     * Default if null.
     */
    private boolean defaultIfNull;

    /**
     * Field column converters. It use for convert java to sql value and sql to java.
     */
    private List<ColumnConverter<?, Object>> columnConverters;

    /**
     * Unique.
     */
    private boolean unique;

    /**
     * Define in create table.
     */
    private boolean defineInCreateTable;

    /**
     * Associated ormnext sql type.
     *
     * @see SqlType
     */
    private SqlType sqlType;

    /**
     * Create a new instance only from {@link #build(Field)} method.
     */
    private SimpleDatabaseColumnTypeImpl() { }

    @Override
    public String defaultDefinition() {
        return defaultDefinition;
    }

    @Override
    public boolean id() {
        return id;
    }

    @Override
    public boolean notNull() {
        return notNull;
    }

    @Override
    public boolean generated() {
        return generated;
    }

    @Override
    public String columnName() {
        return columnName;
    }

    @Override
    public SqlType ormNextSqlType() {
        return sqlType;
    }

    @Override
    public Object access(Object object) {
        return fieldAccessor.access(object);
    }

    @Override
    public DataPersister dataPersister() {
        return dataPersister;
    }

    @Override
    public void assign(Object object, Object value) {
        fieldAccessor.assign(object, value);
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public boolean databaseColumnType() {
        return true;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Optional<List<ColumnConverter<?, Object>>> getColumnConverters() {
        return Optional.ofNullable(columnConverters);
    }

    @Override
    public boolean insertable() {
        return insertable;
    }

    @Override
    public boolean updatable() {
        return updatable;
    }

    @Override
    public boolean defaultIfNull() {
        return defaultIfNull;
    }

    @Override
    public boolean unique() {
        return unique;
    }

    @Override
    public boolean defineInCreateTable() {
        return defineInCreateTable;
    }

    @Override
    public int sqlType() {
        return dataPersister.getSqlType();
    }

    /**
     * Method for build new instance by field.
     * @param field target field
     * @return new instance
     */
    public static SimpleDatabaseColumnTypeImpl build(Field field) {
        if (!field.isAnnotationPresent(DatabaseColumn.class)) {
            return null;
        }
        DatabaseColumn databaseColumn = field.getAnnotation(DatabaseColumn.class);
        SimpleDatabaseColumnTypeImpl columnType = new SimpleDatabaseColumnTypeImpl();

        columnType.field = field;
        columnType.columnName = databaseColumn.columnName().isEmpty()
                ? field.getName().toLowerCase() : databaseColumn.columnName();
        columnType.length = databaseColumn.length();
        columnType.fieldAccessor = new FieldAccessor(field);

        if (!field.isAnnotationPresent(ForeignColumn.class)) {
            DataPersister dataPersister;

            if (databaseColumn.persisterClass().equals(Void.class)) {
                DataType dataType = databaseColumn.dataType();

                dataPersister = dataType.equals(DataType.UNKNOWN)
                        ? DataPersisterManager.lookup(field.getType()) : dataType.getDataPersister();
            } else {
                try {
                    dataPersister = (DataPersister) databaseColumn.persisterClass().newInstance();
                } catch (Exception ex) {
                    throw new DefaultConstructorNotFoundException(databaseColumn.persisterClass());
                }
            }

            columnType.sqlType = dataPersister.getOrmNextSqlType();
            columnType.tableName = DatabaseEntityMetadata.resolveTableName(field.getDeclaringClass());
            columnType.dataPersister = dataPersister;
            String defaultDefinition = databaseColumn.defaultDefinition();

            if (!defaultDefinition.isEmpty()) {
                columnType.defaultDefinition = defaultDefinition;
            }
        }
        if (field.isAnnotationPresent(Converter.class) || field.isAnnotationPresent(Converter.Converters.class)) {
            Converter[] converterGroupAnnotation = field.getAnnotationsByType(Converter.class);

            columnType.columnConverters = new ArrayList<>();
            for (Converter converter : converterGroupAnnotation) {
                columnType.columnConverters.add(toConverter(converter));
            }
        }
        columnType.notNull = databaseColumn.notNull();
        columnType.id = databaseColumn.id();
        columnType.generated = databaseColumn.generated();
        columnType.insertable = databaseColumn.insertable();
        columnType.updatable = databaseColumn.updatable();
        columnType.defaultIfNull = databaseColumn.defaultIfNull();
        columnType.unique = databaseColumn.unique();
        columnType.defineInCreateTable = databaseColumn.defineInCreateTable();

        return columnType;
    }

    /**
     * Make {@link ColumnConverter} from {@link Converter}.
     * @param converter target converter annotation
     * @return column converter instance which created from converter annotation
     */
    private static ColumnConverter<?, Object> toConverter(Converter converter) {
        try {
            List<Class<?>> parametrTypes = new ArrayList<>();

            for (int i = 0; i < converter.args().length; ++i) {
                parametrTypes.add(String.class);
            }
            Constructor<? extends ColumnConverter> constructor =
                    converter.value().getDeclaredConstructor(parametrTypes.toArray(new Class[parametrTypes.size()]));

            return constructor.newInstance(converter.args());
        } catch (Exception ex) {
            throw new InstantiationException(
                    "Converter " + converter.value() + " instantiate exception. " + ex.getMessage(),
                    converter.value(),
                    ex
            );
        }
    }

    @Override
    public void accept(EntityMetadataVisitor visitor) throws SQLException {
        if (visitor.start(this)) {
            visitor.finish(this);
        }
    }
}
