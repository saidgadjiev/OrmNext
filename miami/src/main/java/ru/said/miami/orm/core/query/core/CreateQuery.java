package ru.said.miami.orm.core.query.core;

import ru.said.miami.orm.core.field.DataType;
import ru.said.miami.orm.core.field.FieldType;
import ru.said.miami.orm.core.query.visitor.DefaultVisitor;
import ru.said.miami.orm.core.query.visitor.QueryElement;
import ru.said.miami.orm.core.query.visitor.QueryVisitor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс INSERT запроса
 */
public class CreateQuery implements Query, QueryElement {

    /**
     * Название типа
     */
    private final String typeName;

    /**
     * Вставляемые значения
     */
    private final List<UpdateValue> updateValues = new ArrayList<>();

    private QueryVisitor visitor;

    private CreateQuery(String typeName, QueryVisitor visitor) {
        this.typeName = typeName;
        this.visitor = visitor;
    }

    /**
     * Добавление нового значения
     * @param updateValue добавляемое значение
     */
    public void add(UpdateValue updateValue) {
        updateValues.add(updateValue);
    }

    /**
     * Добавление коллекции значений
     * @param values
     */
    public void addAll(List<UpdateValue> values) {
        updateValues.addAll(values);
    }

    /**
     * Получение списка значений
     * @return
     */
    public List<UpdateValue> getUpdateValues() {
        return updateValues;
    }

    /**
     * Получение имени типа
     * @return
     */
    public String getTypeName() {
        return typeName;
    }

    @Override
    public <T> T execute(Connection connection) throws SQLException {
        this.accept(visitor);
        String sql = visitor.getQuery();

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);

            return (T) new Boolean(true);
        }
    }

    public static CreateQuery buildQuery(String typeName, List<FieldType> fieldTypes, Object object) throws SQLException {
        CreateQuery createQuery = new CreateQuery(typeName, new DefaultVisitor());

        for (FieldType fieldType: fieldTypes) {

            createQuery.updateValues.add(
                    new UpdateValue(
                            fieldType.getFieldName(), FieldConverter.getInstanse().convert(fieldType.getDataType(), fieldType.getValue(object)))
            );
        }

        return createQuery;
    }


    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
        for (UpdateValue updateValue: updateValues) {
            updateValue.accept(visitor);
        }
        visitor.finish(this);
    }
}
