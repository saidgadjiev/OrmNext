package ru.saidgadjiev.orm.next.core.query.core.clause.from;

import ru.saidgadjiev.orm.next.core.query.core.common.TableRef;
import ru.saidgadjiev.orm.next.core.query.core.join.JoinExpression;
import ru.saidgadjiev.orm.next.core.query.visitor.QueryVisitor;

import java.util.ArrayList;
import java.util.List;

public class FromJoinedTables implements FromExpression {

    private TableRef tableRef;

    private List<JoinExpression> joinExpressions = new ArrayList<>();

    public FromJoinedTables(TableRef tableRef) {
        this.tableRef = tableRef;
    }

    public TableRef getTableRef() {
        return tableRef;
    }

    public void addJoinExpression(JoinExpression joinExpression) {
        joinExpressions.add(joinExpression);
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);

    }

    public List<JoinExpression> getJoinExpression() {
        return joinExpressions;
    }
}
