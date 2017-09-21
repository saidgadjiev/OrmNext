package ru.said.miami.orm.core.query.visitor;

/**
 * Паттерн Visitor используется для генерации запроса
 */
public interface QueryElement {

    /**
     * Метод используется для прохода по все нодам визитора
     * @param visitor
     */
    void accept(QueryVisitor visitor);
}
