package ru.said.miami.orm.core.query.core;

import ru.said.miami.orm.core.query.visitor.QueryElement;
import ru.said.miami.orm.core.query.visitor.QueryVisitor;

/**
 * Класс для обновляемого значения
 */
public class UpdateValue implements QueryElement {

    /**
     * Название в таблице обновляемого поля
     */
    private final String name;

    /**
     * Значение обновляемого поля
     */
    private RValue value;

    public UpdateValue(String name, RValue value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Метод возвращает имя поля
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Метод возвращает значение переменной
     * @return value
     */
    public RValue getValue() {
        return value;
    }

    /**
     * Устанавливает значение
     * @param value значение
     */
    public void setValue(RValue value) {
        this.value = value;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
        value.accept(visitor);
        visitor.finish(this);
    }
}
