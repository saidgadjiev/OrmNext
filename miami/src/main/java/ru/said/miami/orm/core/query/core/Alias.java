package ru.said.miami.orm.core.query.core;

import ru.said.miami.orm.core.query.visitor.QueryVisitor;

public class Alias implements Operand {

    private String alias;

    public Alias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
        visitor.finish(this);
    }
}
