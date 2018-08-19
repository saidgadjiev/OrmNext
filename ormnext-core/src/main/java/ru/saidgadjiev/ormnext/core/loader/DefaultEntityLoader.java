package ru.saidgadjiev.ormnext.core.loader;

import ru.saidgadjiev.ormnext.core.connection.DatabaseResults;
import ru.saidgadjiev.ormnext.core.connection.UserDatabaseResultsImpl;
import ru.saidgadjiev.ormnext.core.dao.Dao;
import ru.saidgadjiev.ormnext.core.dao.DatabaseEngine;
import ru.saidgadjiev.ormnext.core.dao.Session;
import ru.saidgadjiev.ormnext.core.exception.GeneratedValueNotFoundException;
import ru.saidgadjiev.ormnext.core.field.fieldtype.DatabaseColumnType;
import ru.saidgadjiev.ormnext.core.field.fieldtype.ForeignCollectionColumnTypeImpl;
import ru.saidgadjiev.ormnext.core.field.fieldtype.ForeignColumnTypeImpl;
import ru.saidgadjiev.ormnext.core.loader.rowreader.resultset.RowResult;
import ru.saidgadjiev.ormnext.core.query.criteria.compiler.StatementCompiler;
import ru.saidgadjiev.ormnext.core.query.criteria.compiler.StatementCompiler.StatementCompileResult;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.DeleteStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.Query;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.UpdateStatement;
import ru.saidgadjiev.ormnext.core.query.space.EntityQuerySpace;
import ru.saidgadjiev.ormnext.core.query.visitor.element.*;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.DatabaseEntityMetadata;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;
import ru.saidgadjiev.ormnext.core.table.internal.persister.DatabaseEntityPersister;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.saidgadjiev.ormnext.core.utils.ArgumentUtils.*;

/**
 * Executes SQL statements via {@link DatabaseEngine}.
 *
 * @author Said Gadjiev
 */
public class DefaultEntityLoader implements EntityLoader {

    /**
     * Current meta model.
     */
    private MetaModel metaModel;

    /**
     * Statement compiler.
     */
    private StatementCompiler statementCompiler;

    /**
     * Current database engine.
     *
     * @see DatabaseEngine
     */
    private DatabaseEngine<?> databaseEngine;

    /**
     * Create a new instance.
     *
     * @param databaseEngine    target database engine
     * @param metaModel         target meta model
     * @param statementCompiler target statement compiler
     */
    public DefaultEntityLoader(DatabaseEngine<?> databaseEngine,
                               MetaModel metaModel,
                               StatementCompiler statementCompiler) {
        this.databaseEngine = databaseEngine;
        this.metaModel = metaModel;
        this.statementCompiler = statementCompiler;
    }

    @Override
    public int create(Session session, Object object) throws SQLException {
        try {
            DatabaseEntityPersister entityPersister = metaModel.getPersister(object.getClass());
            DatabaseEntityMetadata<?> entityMetadata = entityPersister.getMetadata();

            foreignAutoCreate(session, object, entityMetadata);

            Map<DatabaseColumnType, Argument> argumentMap = ejectForCreate(object, entityMetadata);
            EntityQuerySpace entityQuerySpace = entityPersister.getEntityQuerySpace();
            CreateQuery createQuery = entityQuerySpace.getCreateQuery(argumentMap.keySet());
            DatabaseColumnType idField = entityMetadata.getPrimaryKeyColumnType();

            GeneratedKey generatedKey = new KeyHolder();

            int result = databaseEngine.create(
                    session.getConnection(),
                    createQuery,
                    new HashMap<Integer, Argument>() {{
                        AtomicInteger index = new AtomicInteger();

                        argumentMap.forEach((key, value) -> put(index.incrementAndGet(), value));
                    }},
                    idField,
                    generatedKey
            );

            if (idField.generated()) {
                processGeneratedKey(generatedKey, idField, object);
            }

            foreignAutoCreateForeignCollection(session, object, entityMetadata);

            return result;
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public int create(Session session, Object... objects) throws SQLException {
        if (objects.length == 0) {
            return 0;
        }

        try {
            DatabaseEntityPersister entityPersister = metaModel.getPersister(objects[0].getClass());
            DatabaseEntityMetadata<?> entityMetadata = entityPersister.getMetadata();
            EntityQuerySpace entityQuerySpace = entityPersister.getEntityQuerySpace();
            DatabaseColumnType idField = entityMetadata.getPrimaryKeyColumnType();
            CreateQuery createQuery = null;
            List<Map<Integer, Argument>> args = new ArrayList<>();

            for (Object object : objects) {
                Map<DatabaseColumnType, Argument> argumentMap = ejectForCreate(object, entityMetadata);

                if (createQuery == null) {
                    createQuery = entityQuerySpace.getCreateQuery(argumentMap.keySet());
                }
                foreignAutoCreate(session, object, entityMetadata);
                args.add(new HashMap<Integer, Argument>() {{
                    AtomicInteger index = new AtomicInteger();

                    argumentMap.forEach((key, value) -> put(index.incrementAndGet(), value));
                }});
            }

            List<GeneratedKey> generatedKeys = new ArrayList<>();

            for (int i = 0; i < objects.length; ++i) {
                generatedKeys.add(new KeyHolder());
            }
            int result = databaseEngine.create(
                    session.getConnection(),
                    createQuery,
                    args,
                    idField,
                    generatedKeys
            );

            if (idField.generated()) {
                Iterator<GeneratedKey> generatedKeyIterator = generatedKeys.iterator();

                for (Object object : objects) {
                    processGeneratedKey(generatedKeyIterator.next(), idField, object);
                }
            }
            for (Object object : objects) {
                foreignAutoCreateForeignCollection(session, object, entityMetadata);
            }

            return result;
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    /**
     * Process generated key.
     *
     * @param generatedKey target generated key
     * @param idColumnType target id column type
     * @param object       target object
     */
    private void processGeneratedKey(GeneratedKey generatedKey, DatabaseColumnType idColumnType, Object object) {
        if (generatedKey.get() != null) {
            idColumnType.assign(object, idColumnType.dataPersister().convertToPrimaryKey(generatedKey.get()));
        } else {
            throw new GeneratedValueNotFoundException(idColumnType.getField());
        }
    }

    @Override
    public boolean createTable(Session session, Class<?> tClass, boolean ifNotExist) throws SQLException {
        EntityQuerySpace entityQuerySpace = metaModel.getPersister(tClass).getEntityQuerySpace();
        CreateTableQuery createTableQuery = entityQuerySpace.getCreateTableQuery(
                databaseEngine.getDialect(),
                ifNotExist
        );

        return databaseEngine.createTable(session.getConnection(), createTableQuery);
    }

    @Override
    public boolean dropTable(Session session, Class<?> tClass, boolean ifExist) throws SQLException {
        EntityQuerySpace entityQuerySpace = metaModel.getPersister(tClass).getEntityQuerySpace();
        DropTableQuery dropTableQuery = entityQuerySpace.getDropTableQuery(ifExist);

        return databaseEngine.dropTable(session.getConnection(), dropTableQuery);
    }

    @Override
    public int update(Session session, Object object) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(object.getClass());
        DatabaseEntityMetadata<?> entityMetadata = entityPersister.getMetadata();

        Map<DatabaseColumnType, Argument> arguments = ejectForUpdate(object, entityMetadata);
        UpdateQuery updateQuery = entityPersister.getEntityQuerySpace().getUpdateByIdQuery(arguments.keySet());
        DatabaseColumnType primaryKeyColumnType = entityMetadata.getPrimaryKeyColumnType();
        Argument id = eject(object, primaryKeyColumnType);

        arguments.put(primaryKeyColumnType, id);
        try {

            return databaseEngine.update(
                    session.getConnection(),
                    updateQuery,
                    new HashMap<Integer, Argument>() {{
                        AtomicInteger index = new AtomicInteger();

                        arguments.forEach((key, value) -> put(index.incrementAndGet(), value));
                    }}
            );
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public int delete(Session session, Object object) throws SQLException {
        try {
            DatabaseEntityPersister entityPersister = metaModel.getPersister(object.getClass());
            Argument id = eject(object, entityPersister.getMetadata().getPrimaryKeyColumnType());

            DeleteQuery deleteQuery = entityPersister.getEntityQuerySpace().getDeleteByIdQuery();

            return databaseEngine.delete(
                    session.getConnection(),
                    deleteQuery,
                    new HashMap<Integer, Argument>() {{
                        put(1, id);
                    }}
            );
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }

    @Override
    public int deleteById(Session session, Class<?> tClass, Object id) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(tClass);
        DeleteQuery deleteQuery = entityPersister.getEntityQuerySpace().getDeleteByIdQuery();
        Argument idArgument = processConvertersToSqlValue(
                id,
                entityPersister.getMetadata().getPrimaryKeyColumnType()
        );

        return databaseEngine.delete(
                session.getConnection(),
                deleteQuery,
                new HashMap<Integer, Argument>() {{
                    put(1, idArgument);
                }}
        );
    }

    @Override
    public Object queryForId(Session session, Class<?> tClass, Object id) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(tClass);
        EntityQuerySpace entityQuerySpace = entityPersister.getEntityQuerySpace();
        Argument idArgument = processConvertersToSqlValue(
                id,
                entityPersister.getMetadata().getPrimaryKeyColumnType()
        );

        try (DatabaseResults databaseResults = databaseEngine.select(
                session.getConnection(),
                entityQuerySpace.getSelectById(),
                new HashMap<Integer, Argument>() {{
                    put(1, idArgument);
                }}
        )) {
            List<RowResult> rowResults = entityPersister.load(session, databaseResults);

            if (!rowResults.isEmpty()) {
                RowResult rowResult = rowResults.iterator().next();

                return rowResult.getResult();
            }
        }

        return null;
    }

    @Override
    public List<Object> queryForAll(Session session, Class<?> tClass) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(tClass);
        EntityQuerySpace entityQuerySpace = entityPersister.getEntityQuerySpace();

        try (DatabaseResults databaseResults = databaseEngine.select(
                session.getConnection(),
                entityQuerySpace.getSelectAll(),
                Collections.emptyMap()
        )) {
            List<RowResult> results = entityPersister.load(session, databaseResults);

            return results.stream().map(RowResult::getResult).collect(Collectors.toList());
        }
    }

    @Override
    public boolean refresh(Session session, Object object) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(object.getClass());
        DatabaseColumnType primaryKeyType = entityPersister.getMetadata().getPrimaryKeyColumnType();
        EntityQuerySpace entityQuerySpace = entityPersister.getEntityQuerySpace();
        Argument idArgument = eject(object, primaryKeyType);

        try (DatabaseResults databaseResults = databaseEngine.select(
                session.getConnection(),
                entityQuerySpace.getSelectById(),
                new HashMap<Integer, Argument>() {{
                    put(1, idArgument);
                }}
        )) {
            List<RowResult> rowResults = entityPersister.load(session, databaseResults);

            if (!rowResults.isEmpty()) {
                RowResult rowResult = rowResults.iterator().next();
                Object newObject = rowResult.getResult();

                for (DatabaseColumnType columnType : entityPersister.getMetadata().getColumnTypes()) {
                    Object newValue = columnType.access(newObject);

                    columnType.assign(object, newValue);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void createIndexes(Session session, Class<?> tClass) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(tClass);

        for (CreateIndexQuery createIndexQuery : entityPersister.getEntityQuerySpace().getCreateIndexQuery()) {
            databaseEngine.createIndex(session.getConnection(), createIndexQuery);
        }
    }

    @Override
    public void dropIndexes(Session session, Class<?> tClass) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(tClass);

        for (DropIndexQuery dropIndexQuery : entityPersister.getEntityQuerySpace().getDropIndexQuery()) {
            databaseEngine.dropIndex(session.getConnection(), dropIndexQuery);
        }
    }

    @Override
    public long countOff(Session session, Class<?> tClass) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(tClass);
        SelectQuery selectQuery = entityPersister.getEntityQuerySpace().countOff();

        try (DatabaseResults databaseResults = databaseEngine.select(
                session.getConnection(),
                selectQuery,
                new HashMap<>())
        ) {
            if (databaseResults.next()) {
                return databaseResults.getLong(1);
            }
        }

        return 0;
    }

    @Override
    public List<Object> list(Session session, SelectStatement selectStatement) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(selectStatement.getEntityClass());
        StatementCompileResult<SelectQuery> compileResult = statementCompiler.compile(selectStatement);

        try (DatabaseResults databaseResults = databaseEngine.select(
                session.getConnection(),
                compileResult.getQuery(),
                compileResult.getArguments()
        )) {
            List<RowResult> results = entityPersister.load(session, databaseResults);

            return results.stream().map(RowResult::getResult).collect(Collectors.toList());
        }
    }

    @Override
    public long queryForLong(Session session, SelectStatement selectStatement)
            throws SQLException {
        StatementCompileResult<SelectQuery> compileResult = statementCompiler.compile(selectStatement);

        try (DatabaseResults databaseResults = databaseEngine.select(
                session.getConnection(),
                compileResult.getQuery(),
                compileResult.getArguments()
        )) {
            if (databaseResults.next()) {
                return databaseResults.getLong(1);
            }
        }

        return 0;
    }

    @Override
    public DatabaseResults executeQuery(Session session, Query query) throws SQLException {
        return new UserDatabaseResultsImpl(
                null,
                databaseEngine.query(
                        session.getConnection(),
                        query.getQuery()
                )
        );
    }

    @Override
    public int clearTable(Session session, Class<?> entityClass) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(entityClass);
        DeleteQuery clearQuery = entityPersister.getEntityQuerySpace().getDeleteAllQuery();

        return databaseEngine.delete(session.getConnection(), clearQuery, new HashMap<>());
    }

    @Override
    public boolean exist(Session session, Class<?> entityClass, Object id) throws SQLException {
        DatabaseEntityPersister persister = metaModel.getPersister(entityClass);
        SelectQuery existQuery = persister.getEntityQuerySpace().getExistSelect();
        Argument idArgument = processConvertersToSqlValue(
                id,
                persister.getMetadata().getPrimaryKeyColumnType()
        );

        try (DatabaseResults results = databaseEngine.select(
                session.getConnection(),
                existQuery,
                new HashMap<Integer, Argument>() {{
                    put(1, idArgument);
                }}
        )) {
            return results.next() && results.getLong(1) > 0;
        }
    }

    @Override
    public int delete(Session session, DeleteStatement deleteStatement) throws SQLException {
        StatementCompileResult<DeleteQuery> compileResult = statementCompiler.compile(deleteStatement);

        return databaseEngine.delete(
                session.getConnection(),
                compileResult.getQuery(),
                compileResult.getArguments()
        );
    }

    @Override
    public int update(Session session, UpdateStatement updateStatement) throws SQLException {
        StatementCompileResult<UpdateQuery> compileResult = statementCompiler.compile(updateStatement);

        return databaseEngine.update(
                session.getConnection(),
                compileResult.getQuery(),
                compileResult.getArguments()
        );
    }

    @Override
    public int executeUpdate(Session session, Query query) throws SQLException {
        return databaseEngine.executeUpdate(session.getConnection(), query.getQuery());
    }

    @Override
    public DatabaseResults query(Session session, SelectStatement selectStatement) throws SQLException {
        DatabaseEntityPersister entityPersister = metaModel.getPersister(selectStatement.getEntityClass());
        StatementCompileResult<SelectQuery> compileResult = statementCompiler.compile(selectStatement);

        return new UserDatabaseResultsImpl(
                entityPersister.getAliases(),
                databaseEngine.select(
                        session.getConnection(),
                        compileResult.getQuery(),
                        compileResult.getArguments()
                )
        );
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Session session, Object object) throws SQLException {
        DatabaseEntityMetadata<?> metadata = metaModel.getPersister(object.getClass()).getMetadata();
        DatabaseColumnType primaryKeyType = metadata.getPrimaryKeyColumnType();
        Object id = primaryKeyType.access(object);
        Dao.CreateOrUpdateStatus createOrUpdateStatus;

        if (session.exist(object.getClass(), id)) {
            createOrUpdateStatus = new Dao.CreateOrUpdateStatus(true, false, session.update(object));
        } else {
            createOrUpdateStatus = new Dao.CreateOrUpdateStatus(false, true, session.create(object));
        }

        return createOrUpdateStatus;
    }

    /**
     * Foreign column
     * {@link ru.saidgadjiev.ormnext.core.field.ForeignColumn} object auto create if auto create enabled.
     *
     * @param session        target session
     * @param object         target object which contains foreign columns
     * @param entityMetadata target entity metadata
     * @throws SQLException any SQL exceptions
     */
    private void foreignAutoCreate(Session session,
                                   Object object,
                                   DatabaseEntityMetadata<?> entityMetadata
    ) throws SQLException {
        for (ForeignColumnTypeImpl foreignColumnType : entityMetadata.toForeignColumnTypes()) {
            Object foreignObject = foreignColumnType.access(object);

            if (foreignObject != null && foreignColumnType.foreignAutoCreate()) {
                session.create(foreignObject);
            }
        }
    }

    /**
     * Create objects from foreign collection field
     * {@link ru.saidgadjiev.ormnext.core.field.ForeignCollectionField} if enable auto create.
     *
     * @param session        target session
     * @param object         target object which contains foreign collection fields
     * @param entityMetadata target metadata.
     * @throws SQLException any SQL exceptions
     */
    private void foreignAutoCreateForeignCollection(Session session,
                                                    Object object,
                                                    DatabaseEntityMetadata<?> entityMetadata
    ) throws SQLException {
        for (ForeignCollectionColumnTypeImpl foreignCollectionColumnType
                : entityMetadata.toForeignCollectionColumnTypes()) {
            if (foreignCollectionColumnType.foreignAutoCreate()) {
                for (Object foreignObject : foreignCollectionColumnType.access(object)) {
                    foreignCollectionColumnType.getForeignColumnType().assign(foreignObject, object);

                    session.create(foreignObject);
                }
            }
        }
    }

    /**
     * Generated key holder implementation.
     */
    private static class KeyHolder implements GeneratedKey {

        /**
         * Generated key.
         */
        private Object key;

        @Override
        public void set(Object key) throws SQLException {
            if (this.key != null) {
                throw new SQLException(
                        "Generated key has already been set to " + this.key + ", now set to " + key
                );
            }

            this.key = key;
        }

        @Override
        public Object get() {
            return key;
        }
    }
}
