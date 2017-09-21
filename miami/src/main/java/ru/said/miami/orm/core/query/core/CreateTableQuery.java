package ru.said.miami.orm.core.query.core;

import ru.said.miami.orm.core.field.FieldType;
import ru.said.miami.orm.core.query.visitor.DefaultVisitor;
import ru.said.miami.orm.core.query.visitor.QueryElement;
import ru.said.miami.orm.core.query.visitor.QueryVisitor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CreateTableQuery implements Query, QueryElement {

    private List<AttributeDefenition> attributeDefenitions = new ArrayList<>();

    private String typeName;

    private QueryVisitor visitor;

    private CreateTableQuery(String typeName, List<AttributeDefenition> attributeDefenitions, QueryVisitor defaultVisitor) {
        this.visitor = defaultVisitor;
        this.typeName = typeName;
        this.attributeDefenitions = attributeDefenitions;
    }

    public String getTypeName() {
        return typeName;
    }

    public List<AttributeDefenition> getAttributeDefenitions() {
        return attributeDefenitions;
    }

    public static CreateTableQuery buildQuery(String typeName, List<FieldType> fieldTypes) {
        List<AttributeDefenition> attributeDefenitions = new ArrayList<>();

        for (FieldType fieldType: fieldTypes) {
            attributeDefenitions.add(
                    new AttributeDefenition(
                            fieldType.getFieldName(),
                            fieldType.getDataType(),
                            fieldType.getLength(),
                            fieldType.isId(),
                            fieldType.isGenerated()));
        }

        return new CreateTableQuery(typeName, attributeDefenitions, new DefaultVisitor());
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.start(this);
        for (AttributeDefenition attributeDefenition: attributeDefenitions) {
            attributeDefenition.accept(visitor);
        }
        visitor.finish(this);
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
}
