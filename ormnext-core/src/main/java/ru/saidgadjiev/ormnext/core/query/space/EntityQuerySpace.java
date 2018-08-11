package ru.saidgadjiev.ormnext.core.query.space;

import ru.saidgadjiev.ormnext.core.dialect.Dialect;
import ru.saidgadjiev.ormnext.core.field.fieldtype.DatabaseColumnType;
import ru.saidgadjiev.ormnext.core.field.fieldtype.ForeignCollectionColumnTypeImpl;
import ru.saidgadjiev.ormnext.core.field.fieldtype.ForeignColumnTypeImpl;
import ru.saidgadjiev.ormnext.core.loader.Argument;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.DeleteStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.UpdateStatement;
import ru.saidgadjiev.ormnext.core.query.visitor.element.*;
import ru.saidgadjiev.ormnext.core.query.visitor.element.clause.from.FromJoinedTables;
import ru.saidgadjiev.ormnext.core.query.visitor.element.clause.from.FromTable;
import ru.saidgadjiev.ormnext.core.query.visitor.element.clause.select.SelectColumnsList;
import ru.saidgadjiev.ormnext.core.query.visitor.element.columnspec.ColumnSpec;
import ru.saidgadjiev.ormnext.core.query.visitor.element.columnspec.DisplayedColumn;
import ru.saidgadjiev.ormnext.core.query.visitor.element.columnspec.DisplayedColumnSpec;
import ru.saidgadjiev.ormnext.core.query.visitor.element.columnspec.DisplayedOperand;
import ru.saidgadjiev.ormnext.core.query.visitor.element.common.TableRef;
import ru.saidgadjiev.ormnext.core.query.visitor.element.common.UpdateValue;
import ru.saidgadjiev.ormnext.core.query.visitor.element.condition.Equals;
import ru.saidgadjiev.ormnext.core.query.visitor.element.condition.Expression;
import ru.saidgadjiev.ormnext.core.query.visitor.element.constraints.attribute.Default;
import ru.saidgadjiev.ormnext.core.query.visitor.element.constraints.attribute.NotNullConstraint;
import ru.saidgadjiev.ormnext.core.query.visitor.element.constraints.attribute.ReferencesConstraint;
import ru.saidgadjiev.ormnext.core.query.visitor.element.constraints.attribute.UniqueAttributeConstraint;
import ru.saidgadjiev.ormnext.core.query.visitor.element.constraints.table.ForeignKeyConstraint;
import ru.saidgadjiev.ormnext.core.query.visitor.element.constraints.table.UniqueConstraint;
import ru.saidgadjiev.ormnext.core.query.visitor.element.function.CountAll;
import ru.saidgadjiev.ormnext.core.query.visitor.element.join.LeftJoin;
import ru.saidgadjiev.ormnext.core.query.visitor.element.literals.Param;
import ru.saidgadjiev.ormnext.core.table.internal.alias.EntityAliases;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.DatabaseEntityMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query space for entity. It contains prepared join expression, resolved aliases and select columns list
 * and methods for create statements for different operations eg. select by id, select all...
 *
 * @author Said Gadjiev
 */
public class EntityQuerySpace {

    /**
     * SelectQuery columns.
     *
     * @see SelectColumnsList
     */
    private SelectColumnsList selectColumnsList = new SelectColumnsList();

    /**
     * Join expression.
     *
     * @see FromJoinedTables
     */
    private FromJoinedTables fromJoinedTables;

    /**
     * Table aliases.
     *
     * @see EntityAliases
     */
    private EntityAliases rootEntityAliases;

    /**
     * Table meta data.
     *
     * @see DatabaseEntityMetadata
     */
    private DatabaseEntityMetadata<?> rootEntityMetaData;

    /**
     * Create a new instance.
     *
     * @param rootEntityMetaData root table meta data
     * @param rootEntityAliases  root table aliases
     */
    public EntityQuerySpace(DatabaseEntityMetadata<?> rootEntityMetaData, EntityAliases rootEntityAliases) {
        this.rootEntityMetaData = rootEntityMetaData;
        this.rootEntityAliases = rootEntityAliases;
        TableRef tableRef = new TableRef(rootEntityMetaData.getTableName(), rootEntityAliases.getTableAlias());

        fromJoinedTables = new FromJoinedTables(tableRef);
    }

    /**
     * Append join.
     *
     * @param foreignColumnType target column
     * @param ownerAliases      owner aliases
     * @param joinTableAliases  join table aliases
     */
    public void appendJoin(ForeignColumnTypeImpl foreignColumnType,
                           EntityAliases ownerAliases,
                           EntityAliases joinTableAliases) {
        Expression onExpression = new Expression();
        AndCondition andCondition = new AndCondition();
        DatabaseColumnType foreignDatabaseColumnType = foreignColumnType.getForeignDatabaseColumnType();

        andCondition.add(
                new Equals(
                        new ColumnSpec(foreignColumnType.columnName(), ownerAliases.getTableAlias()),
                        new ColumnSpec(foreignDatabaseColumnType.columnName(), joinTableAliases.getTableAlias())
                )
        );
        onExpression.add(andCondition);
        TableRef joinedTableRef = new TableRef(
                foreignColumnType.getForeignTableName(),
                joinTableAliases.getTableAlias()
        );

        fromJoinedTables.add(new LeftJoin(joinedTableRef, onExpression));
    }

    /**
     * Append join for {@link ru.saidgadjiev.ormnext.core.field.ForeignCollectionField} column.
     *
     * @param foreignCollectionColumnType column type
     * @param ownerAliases                owner aliases
     * @param joinTableAliases            join table aliases
     */
    public void appendCollectionJoin(ForeignCollectionColumnTypeImpl foreignCollectionColumnType,
                                     EntityAliases ownerAliases,
                                     EntityAliases joinTableAliases) {
        Expression onExpression = new Expression();
        AndCondition andCondition = new AndCondition();

        andCondition.add(
                new Equals(
                        new ColumnSpec(
                                foreignCollectionColumnType.getForeignColumnType().getForeignColumnName(),
                                ownerAliases.getTableAlias()
                        ),
                        new ColumnSpec(
                                foreignCollectionColumnType.getForeignColumnName(),
                                joinTableAliases.getTableAlias()
                        )
                )
        );
        onExpression.add(andCondition);
        TableRef joinedTableRef = new TableRef(
                foreignCollectionColumnType.getForeignTableName(),
                joinTableAliases.getTableAlias()
        );

        fromJoinedTables.add(new LeftJoin(joinedTableRef, onExpression));
    }

    /**
     * Append select columns.
     *
     * @param aliases                table aliases
     * @param databaseEntityMetadata table meta data
     */
    public void appendSelectColumns(EntityAliases aliases, DatabaseEntityMetadata<?> databaseEntityMetadata) {
        List<DatabaseColumnType> columnTypes = databaseEntityMetadata.getDisplayedColumnTypes();
        List<String> columnAliases = aliases.getColumnAliases();

        for (int i = 0; i < columnTypes.size(); ++i) {
            if (columnTypes.get(i).foreignCollectionColumnType()) {
                continue;
            }
            DatabaseColumnType columnType = columnTypes.get(i);

            if (columnTypes.get(i).id()) {
                appendDisplayedColumn(columnType.columnName(), aliases.getTableAlias(), aliases.getKeyAlias());
                continue;
            }
            appendDisplayedColumn(columnType.columnName(), aliases.getTableAlias(), columnAliases.get(i));
        }
    }

    /**
     * Append column spec.
     *
     * @param columnName  column name
     * @param tableAlias  table alias
     * @param columnAlias column alias
     */
    private void appendDisplayedColumn(String columnName, String tableAlias, String columnAlias) {
        ColumnSpec columnSpec = new ColumnSpec(columnName, tableAlias);
        DisplayedColumnSpec displayedColumnSpec = new DisplayedColumn(columnSpec);

        displayedColumnSpec.setAlias(new Alias(columnAlias));
        selectColumnsList.addColumn(displayedColumnSpec);
    }

    /**
     * Make and return select by id.
     *
     * @return select statement
     */
    public SelectQuery getSelectById() {
        SelectQuery selectById = new SelectQuery();

        selectById.setSelectColumnsStrategy(selectColumnsList);
        selectById.setFrom(fromJoinedTables);
        AndCondition andCondition = new AndCondition();
        Expression where = new Expression();
        ColumnSpec idColumnSpec = new ColumnSpec(
                rootEntityMetaData.getPrimaryKeyColumnType().columnName(),
                rootEntityAliases.getTableAlias()
        );

        andCondition.add(new Equals(idColumnSpec, new Param()));
        where.getConditions().add(andCondition);
        selectById.setWhere(where);

        return selectById;
    }

    /**
     * Make and return select all.
     *
     * @return select statement
     */
    public SelectQuery getSelectAll() {
        SelectQuery selectAll = new SelectQuery();

        selectAll.setSelectColumnsStrategy(selectColumnsList);
        selectAll.setFrom(fromJoinedTables);

        return selectAll;
    }

    /**
     * Make and return create statement which prepared for use in jdbc prepared statement.
     *
     * @param resultColumnTypes result columns in statement
     * @return create statement
     */
    public CreateQuery getCreateQuery(Collection<DatabaseColumnType> resultColumnTypes) {
        CreateQuery createQuery = new CreateQuery(rootEntityMetaData.getTableName());

        for (DatabaseColumnType columnType : resultColumnTypes) {
            if (columnType.id() && columnType.generated()) {
                continue;
            }
            if (columnType.foreignCollectionColumnType()) {
                continue;
            }
            createQuery.add(new UpdateValue(columnType.columnName(), new Param()));
        }

        return createQuery;
    }

    /**
     * Return compiled insert statement.
     *
     * @param argumentMap target argument map
     * @return compiled insert statement
     */
    public CreateQuery getCreateQueryCompiledStatement(Map<DatabaseColumnType, Argument> argumentMap) {
        CreateQuery createQuery = new CreateQuery(rootEntityMetaData.getTableName());

        for (Map.Entry<DatabaseColumnType, Argument> entry : argumentMap.entrySet()) {
            DatabaseColumnType columnType = entry.getKey();

            createQuery.add(
                    new UpdateValue(
                            columnType.columnName(),
                            columnType.dataPersister().createLiteral(entry.getValue().getValue())
                    )
            );
        }

        return createQuery;
    }

    /**
     * Make and return create table statement.
     *
     * @param ifNotExist append if not exist
     * @param dialect    target database dialect {@link Dialect}
     * @return create table statement
     */
    public CreateTableQuery getCreateTableQuery(Dialect dialect, boolean ifNotExist) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        CreateTableQuery createTableQuery = new CreateTableQuery(
                rootEntityMetaData.getTableName(),
                ifNotExist,
                attributeDefinitions
        );

        for (DatabaseColumnType columnType : rootEntityMetaData.getColumnTypes()) {
            if (columnType.foreignCollectionColumnType() || !columnType.defineInCreateTable()) {
                continue;
            }
            AttributeDefinition attributeDefinition = new AttributeDefinition(
                    columnType
            );

            if (columnType.notNull()) {
                attributeDefinition.getAttributeConstraints().add(new NotNullConstraint());
            }
            if (columnType.defaultDefinition() != null) {
                attributeDefinition.getAttributeConstraints().add(new Default(columnType.defaultDefinition()));
            }
            if (columnType.unique()) {
                attributeDefinition.getAttributeConstraints().add(new UniqueAttributeConstraint());
            }
            if (columnType.foreignColumnType()) {
                ForeignColumnTypeImpl foreignColumnType = (ForeignColumnTypeImpl) columnType;

                if (!dialect.supportTableForeignConstraint()) {
                    attributeDefinition.getAttributeConstraints().add(
                            new ReferencesConstraint(
                                    foreignColumnType.getForeignTableName(),
                                    foreignColumnType.columnName(),
                                    foreignColumnType.getOnDelete(),
                                    foreignColumnType.getOnUpdate())
                    );
                } else {
                    createTableQuery
                            .getTableConstraints()
                            .add(new ForeignKeyConstraint(
                                            foreignColumnType.getForeignTableName(),
                                            foreignColumnType.getForeignColumnName(),
                                            foreignColumnType.columnName(),
                                            foreignColumnType.getOnDelete(),
                                            foreignColumnType.getOnUpdate()
                                    )
                            );
                }
            }
            attributeDefinitions.add(attributeDefinition);
        }

        if (dialect.supportTableUniqueConstraint()) {
            createTableQuery.getTableConstraints().addAll(rootEntityMetaData.getUniqueColumns()
                    .stream()
                    .map(UniqueConstraint::new)
                    .collect(Collectors.toList()));
        }

        return createTableQuery;
    }

    /**
     * Make and return drop table statement.
     *
     * @param ifExist append if exist
     * @return drop table statement
     */
    public DropTableQuery getDropTableQuery(boolean ifExist) {
        return new DropTableQuery(
                rootEntityMetaData.getTableName(),
                ifExist
        );
    }

    /**
     * Make and return update statement.
     *
     * @param argumentMap target argument map
     * @param id          target object id argument
     * @return update statement
     */
    public UpdateQuery getUpdateByIdCompiledQuery(Map<DatabaseColumnType, Argument> argumentMap,
                                                  Argument id) {
        UpdateQuery updateQuery = new UpdateQuery(rootEntityMetaData.getTableName());

        for (Map.Entry<DatabaseColumnType, Argument> entry : argumentMap.entrySet()) {
            DatabaseColumnType columnType = entry.getKey();

            updateQuery.add(
                    new UpdateValue(
                            columnType.columnName(),
                            columnType.dataPersister().createLiteral(entry.getValue().getValue()))
            );
        }
        DatabaseColumnType primaryKeyType = rootEntityMetaData.getPrimaryKeyColumnType();
        AndCondition andCondition = new AndCondition();
        ColumnSpec idColumnSpec = new ColumnSpec(
                primaryKeyType.columnName(),
                rootEntityMetaData.getTableName()
        );

        andCondition.add(new Equals(idColumnSpec, primaryKeyType.dataPersister().createLiteral(id.getValue())));
        updateQuery.getWhere().getConditions().add(andCondition);

        return updateQuery;
    }

    /**
     * Make and return update statement.
     *
     * @param updateColumnTypes update columns
     * @return update statement
     */
    public UpdateQuery getUpdateByIdQuery(Collection<DatabaseColumnType> updateColumnTypes) {
        UpdateQuery updateQuery = new UpdateQuery(rootEntityMetaData.getTableName());

        for (DatabaseColumnType fieldType : updateColumnTypes) {
            updateQuery.add(
                    new UpdateValue(
                            fieldType.columnName(),
                            new Param())
            );
        }
        AndCondition andCondition = new AndCondition();
        ColumnSpec idColumnSpec = new ColumnSpec(
                rootEntityMetaData.getPrimaryKeyColumnType().columnName(),
                rootEntityMetaData.getTableName()
        );

        andCondition.add(new Equals(idColumnSpec, new Param()));
        updateQuery.getWhere().getConditions().add(andCondition);

        return updateQuery;
    }

    /**
     * Make and return delete statement.
     *
     * @return delete statement
     */
    public DeleteQuery getDeleteByIdQuery() {
        DeleteQuery deleteQuery = new DeleteQuery(rootEntityMetaData.getTableName());
        AndCondition andCondition = new AndCondition();
        ColumnSpec idColumnSpec = new ColumnSpec(
                rootEntityMetaData.getPrimaryKeyColumnType().columnName(),
                rootEntityMetaData.getTableName()
        );

        andCondition.add(new Equals(idColumnSpec, new Param()));
        deleteQuery.getWhere().getConditions().add(andCondition);

        return deleteQuery;
    }

    /**
     * Delete all executeQuery from table.
     *
     * @return delete all statement
     */
    public DeleteQuery getDeleteAllQuery() {
        return new DeleteQuery(rootEntityMetaData.getTableName());
    }

    /**
     * Make and return compiled delete executeQuery.
     *
     * @param id target object id which will be deleted
     * @return compiled delete executeQuery
     */
    public DeleteQuery getDeleteByIdCompiledQuery(Argument id) {
        DeleteQuery deleteQuery = new DeleteQuery(rootEntityMetaData.getTableName());
        DatabaseColumnType columnType = rootEntityMetaData.getPrimaryKeyColumnType();
        AndCondition andCondition = new AndCondition();
        ColumnSpec idColumnSpec = new ColumnSpec(
                rootEntityMetaData.getPrimaryKeyColumnType().columnName(),
                rootEntityMetaData.getTableName()
        );

        andCondition.add(new Equals(idColumnSpec, columnType.dataPersister().createLiteral(id.getValue())));
        deleteQuery.getWhere().getConditions().add(andCondition);

        return deleteQuery;
    }

    /**
     * Make and return create index statement.
     *
     * @return create index statement
     */
    public Collection<CreateIndexQuery> getCreateIndexQuery() {
        return rootEntityMetaData.getIndexColumns()
                .stream()
                .map(CreateIndexQuery::new)
                .collect(Collectors.toList());
    }

    /**
     * Make and return drop index statement.
     *
     * @return drop index statement
     */
    public Collection<DropIndexQuery> getDropIndexQuery() {
        return rootEntityMetaData.getIndexColumns()
                .stream()
                .map(indexFieldType -> new DropIndexQuery(indexFieldType.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Make and return select count star statement.
     *
     * @return select count star statement
     */
    public SelectQuery countOff() {
        SelectQuery selectQuery = new SelectQuery();
        SelectColumnsList selectColumnsList = new SelectColumnsList();

        selectQuery.setFrom(new FromTable(new TableRef(rootEntityMetaData.getTableName())));
        selectColumnsList.addColumn(new DisplayedOperand(new CountAll()));
        selectQuery.setSelectColumnsStrategy(selectColumnsList);

        return selectQuery;
    }

    /**
     * Make and return select statement.
     *
     * @param selectStatement target select executeQuery
     * @return select statement
     */
    public SelectQuery getSelectQuery(SelectStatement<?> selectStatement) {
        SelectQuery selectQuery = new SelectQuery();

        if (selectStatement.getSelectOperands().isEmpty()) {
            selectQuery.setSelectColumnsStrategy(selectColumnsList);
        } else {
            SelectColumnsList selectColumnsList = new SelectColumnsList();

            selectColumnsList.addAll(selectStatement.getSelectOperands());

            selectQuery.setSelectColumnsStrategy(selectColumnsList);
        }
        if (selectStatement.isWithoutJoins()) {
            selectQuery.setFrom(
                    new FromTable(
                            new TableRef(rootEntityMetaData.getTableName()).alias(rootEntityAliases.getTableAlias())
                    )
            );
        } else {
            selectQuery.setFrom(fromJoinedTables);
        }
        selectQuery.setWhere(selectStatement.getWhere());
        selectQuery.setGroupBy(selectStatement.getGroupBy());
        selectQuery.setOrderBy(selectStatement.getOrderBy());
        selectQuery.setHaving(selectStatement.getHaving());
        selectQuery.setLimit(selectStatement.getLimit());
        selectQuery.setOffset(selectStatement.getOffset());

        selectQuery.accept(new ResolvePropertyColumn(rootEntityMetaData));
        selectQuery.accept(new SetPropertyColumnAliases(rootEntityAliases));

        return selectQuery;
    }

    /**
     * Make and return exist row with id select executeQuery.
     *
     * @return exist row with id select executeQuery
     */
    public SelectQuery getExistSelect() {
        SelectQuery select = new SelectQuery();

        select.setFrom(new FromTable(new TableRef(rootEntityMetaData.getTableName())));

        SelectColumnsList selectColumnsStrategy = new SelectColumnsList();

        selectColumnsStrategy.addColumn(new DisplayedOperand(new CountAll()));

        select.setSelectColumnsStrategy(selectColumnsStrategy);

        AndCondition andCondition = new AndCondition();

        andCondition.add(
                new Equals(
                        new ColumnSpec(rootEntityMetaData.getPrimaryKeyColumnType().columnName()),
                        new Param()
                )
        );
        Expression expression = new Expression();

        expression.add(andCondition);
        select.setWhere(expression);

        return select;
    }

    /**
     * Create delete executeQuery from delete statement.
     *
     * @param deleteStatement target delete statement
     * @return return delete executeQuery
     */
    public DeleteQuery getDeleteQuery(DeleteStatement deleteStatement) {
        DeleteQuery deleteQuery = new DeleteQuery(rootEntityMetaData.getTableName());

        deleteQuery.setWhere(deleteStatement.getWhere());

        deleteQuery.accept(new ResolvePropertyColumn(rootEntityMetaData));

        return deleteQuery;
    }

    /**
     * Create update executeQuery from update statement.
     *
     * @param updateStatement target update statement
     * @return return update executeQuery
     */
    public UpdateQuery getUpdateQuery(UpdateStatement updateStatement) {
        UpdateQuery updateQuery = new UpdateQuery(rootEntityMetaData.getTableName());

        updateQuery.setWhere(updateStatement.getWhere());

        updateQuery.addAll(updateStatement.getUpdateValues());

        updateQuery.accept(new ResolvePropertyColumn(rootEntityMetaData));

        return updateQuery;
    }
}
