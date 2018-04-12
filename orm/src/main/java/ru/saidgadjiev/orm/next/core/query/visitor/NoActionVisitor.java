package ru.saidgadjiev.orm.next.core.query.visitor;

import ru.saidgadjiev.orm.next.core.query.core.*;
import ru.saidgadjiev.orm.next.core.query.core.clause.*;
import ru.saidgadjiev.orm.next.core.query.core.clause.from.FromJoinedTables;
import ru.saidgadjiev.orm.next.core.query.core.clause.from.FromSubQuery;
import ru.saidgadjiev.orm.next.core.query.core.clause.from.FromTable;
import ru.saidgadjiev.orm.next.core.query.core.clause.select.SelectAll;
import ru.saidgadjiev.orm.next.core.query.core.clause.select.SelectColumnsList;
import ru.saidgadjiev.orm.next.core.query.core.column_spec.ColumnSpec;
import ru.saidgadjiev.orm.next.core.query.core.column_spec.DisplayedColumn;
import ru.saidgadjiev.orm.next.core.query.core.column_spec.DisplayedOperand;
import ru.saidgadjiev.orm.next.core.query.core.common.TableRef;
import ru.saidgadjiev.orm.next.core.query.core.common.UpdateValue;
import ru.saidgadjiev.orm.next.core.query.core.condition.*;
import ru.saidgadjiev.orm.next.core.query.core.constraints.attribute.Default;
import ru.saidgadjiev.orm.next.core.query.core.constraints.attribute.NotNullConstraint;
import ru.saidgadjiev.orm.next.core.query.core.constraints.attribute.PrimaryKeyConstraint;
import ru.saidgadjiev.orm.next.core.query.core.constraints.attribute.ReferencesConstraint;
import ru.saidgadjiev.orm.next.core.query.core.constraints.table.ForeignKeyConstraint;
import ru.saidgadjiev.orm.next.core.query.core.constraints.table.UniqueConstraint;
import ru.saidgadjiev.orm.next.core.query.core.function.*;
import ru.saidgadjiev.orm.next.core.query.core.join.JoinInfo;
import ru.saidgadjiev.orm.next.core.query.core.join.LeftJoin;
import ru.saidgadjiev.orm.next.core.query.core.literals.*;

/**
 * Created by said on 23.02.2018.
 */
public class NoActionVisitor implements QueryVisitor {

    @Override
    public boolean visit(CreateQuery tCreateQuery) {
        return true;
    }

    @Override
    public boolean visit(UpdateValue updateValue) {
        return true;
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
    }

    @Override
    public boolean visit(Select tSelectQuery) {
        return true;
    }

    @Override
    public boolean visit(Expression expression) {
        return true;
    }

    @Override
    public boolean visit(AndCondition andCondition) {
        return true;
    }

    @Override
    public boolean visit(Equals equals) {
        return true;
    }

    @Override
    public boolean visit(ColumnSpec columnSpec) {
        return true;
    }

    @Override
    public boolean visit(TableRef tableRef) {
        return true;
    }

    @Override
    public boolean visit(AttributeDefinition attributeDefinition) {
        return true;
    }

    @Override
    public boolean visit(CreateTableQuery tCreateTableQuery) {
        return true;
    }

    @Override
    public boolean visit(DeleteQuery deleteQuery) {
        return true;
    }

    @Override
    public void visit(IntLiteral intLiteral) {
    }

    @Override
    public boolean visit(UpdateQuery updateQuery) {
        return true;
    }

    @Override
    public void visit(DropTableQuery dropTableQuery) {
    }

    @Override
    public void visit(PrimaryKeyConstraint primaryKeyConstraint) {
    }

    @Override
    public void visit(UniqueConstraint uniqueConstraint) {
    }

    @Override
    public void visit(NotNullConstraint notNullConstraint) {
    }

    @Override
    public void visit(ReferencesConstraint referencesConstraint) {
    }

    @Override
    public void visit(CreateIndexQuery createIndexQuery) {
    }

    @Override
    public void visit(DropIndexQuery dropIndexQuery) {
    }

    @Override
    public void visit(Param param) {
    }

    @Override
    public void visit(SelectAll selectAll) {
    }

    @Override
    public boolean visit(SelectColumnsList selectColumnsList) {
        return true;
    }

    @Override
    public boolean visit(Having having) {
        return true;
    }

    @Override
    public boolean visit(GroupBy groupBy) {
        return true;
    }

    @Override
    public boolean visit(FromTable fromTable) {
        return true;
    }

    @Override
    public boolean visit(LeftJoin leftJoin) {
        return true;
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
    }

    @Override
    public boolean visit(JoinInfo joinInfo) {
        return true;
    }

    @Override
    public void visit(CountAll countAll) {
    }

    @Override
    public boolean visit(FromJoinedTables fromJoinedTables) {
        return true;
    }

    @Override
    public boolean visit(DisplayedColumn displayedColumn) {
        return true;
    }

    @Override
    public boolean visit(AVG avg) {
        return true;
    }

    @Override
    public boolean visit(CountExpression countExpression) {
        return true;
    }

    @Override
    public boolean visit(MAX max) {
        return true;
    }

    @Override
    public boolean visit(MIN min) {
        return true;
    }

    @Override
    public boolean visit(Exists exists) {
        return true;
    }

    @Override
    public boolean visit(InSelect inSelect) {
        return true;
    }

    @Override
    public boolean visit(NotInSelect notInSelect) {
        return true;
    }

    @Override
    public boolean visit(GreaterThan greaterThan) {
        return true;
    }

    @Override
    public boolean visit(GreaterThanOrEquals greaterThanOrEquals) {
        return true;
    }

    @Override
    public boolean visit(LessThan lessThan) {
        return true;
    }

    @Override
    public boolean visit(LessThanOrEquals lessThanOrEquals) {
        return true;
    }

    @Override
    public boolean visit(SUM sum) {
        return true;
    }

    @Override
    public boolean visit(OperandCondition operandCondition) {
        return true;
    }

    @Override
    public void visit(Alias alias) {
    }

    @Override
    public void visit(DateLiteral dateLiteral) {
    }

    @Override
    public boolean visit(Default aDefault) {
        return true;
    }

    @Override
    public boolean visit(DisplayedOperand displayedOperand) {
        return true;
    }

    @Override
    public void visit(FloatLiteral floatLiteral) {
    }

    @Override
    public void visit(DoubleLiteral doubleLiteral) {
    }

    @Override
    public boolean visit(OrderBy orderBy) {
        return true;
    }

    @Override
    public boolean visit(OrderByItem orderByItem, QueryVisitor visitor) {
        return true;
    }

    @Override
    public void visit(Limit limit) {
    }

    @Override
    public void visit(Offset offset) {
    }

    @Override
    public boolean visit(NotNull notNull) {
        return true;
    }

    @Override
    public boolean visit(IsNull isNull) {
        return true;
    }

    @Override
    public boolean visit(NotEquals notEquals) {
        return true;
    }

    @Override
    public boolean visit(Like like) {
        return true;
    }

    @Override
    public boolean visit(Between between) {
        return true;
    }

    @Override
    public boolean visit(Not not) {
        return true;
    }

    @Override
    public boolean visit(NotInValues notInValues) {
        return true;
    }

    @Override
    public boolean visit(InValues inValues) {
        return true;
    }

    @Override
    public boolean visit(ForeignKeyConstraint foreignKeyConstraint) {
        return true;
    }

    @Override
    public boolean visit(FromSubQuery fromSubQuery) {
        return true;
    }

    @Override
    public boolean visit(GroupByItem groupByItem) {
        return true;
    }

    @Override
    public boolean visit(OrderByItem orderByItem) {
        return true;
    }
}
