package ru.said.orm.next.core.stament_executor;

import ru.said.orm.next.core.field.DataType;
import ru.said.orm.next.core.query.core.literals.IntLiteral;
import ru.said.orm.next.core.query.core.literals.RValue;
import ru.said.orm.next.core.query.core.literals.StringLiteral;

public class FieldConverter {

    private static final FieldConverter INSTANSE = new FieldConverter();

    private FieldConverter() {}

    public static FieldConverter getInstanse() {
        return INSTANSE;
    }

    public RValue convert(DataType dataType, Object value) {
        switch (dataType) {
            case STRING:
                return new StringLiteral((String) value);
            case INTEGER:
                return new IntLiteral((Integer) value);
            case UNKNOWN:
                return null;
            default:
                return null;
        }
    }
}