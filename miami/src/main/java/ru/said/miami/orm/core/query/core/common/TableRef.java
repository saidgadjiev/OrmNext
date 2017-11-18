package ru.said.miami.orm.core.query.core.common;

import ru.said.miami.orm.core.query.core.Alias;
import ru.said.miami.orm.core.query.core.columnSpec.IHasAlias;
import ru.said.miami.orm.core.query.visitor.QueryElement;
import ru.said.miami.orm.core.query.visitor.QueryVisitor;

/**
 * Created by said on 09.09.17.
 */
public class TableRef implements QueryElement {

    private String tableName;

    private Alias alias;

    public TableRef(String tableName) {
        this.tableName = tableName;
    }

    public TableRef alias(Alias alias) {
        this.alias = alias;

        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public Alias getAlias() {
        return alias;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
        visitor.finish(this);
    }
}

