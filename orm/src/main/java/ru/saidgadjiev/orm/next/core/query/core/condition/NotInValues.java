package ru.saidgadjiev.orm.next.core.query.core.condition;

import ru.saidgadjiev.orm.next.core.query.core.Operand;
import ru.saidgadjiev.orm.next.core.query.core.literals.RValue;
import ru.saidgadjiev.orm.next.core.query.visitor.QueryVisitor;

import java.util.ArrayList;
import java.util.List;

public class NotInValues implements Condition {

    private final List<RValue> values = new ArrayList<>();

    private final Operand operand;

    public NotInValues(Operand operand) {
        this.operand = operand;
    }

    public void addValue(RValue literal) {
        this.values.add(literal);
    }

    public List<RValue> getValues() {
        return this.values;
    }

    public Operand getOperand() {
        return this.operand;
    }

    public void accept(QueryVisitor visitor) {
       visitor.visit(this);
    }
}