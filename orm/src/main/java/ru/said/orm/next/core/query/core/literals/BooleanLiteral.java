package ru.said.orm.next.core.query.core.literals;

import ru.said.orm.next.core.query.visitor.QueryVisitor;

public class BooleanLiteral implements Literal<Boolean> {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public String getOriginal() {
        return String.valueOf(value);
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
        visitor.finish(this);
    }
}