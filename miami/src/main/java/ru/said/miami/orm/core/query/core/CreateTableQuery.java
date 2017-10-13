package ru.said.miami.orm.core.query.core;

import ru.said.miami.orm.core.field.DBFieldType;
import ru.said.miami.orm.core.query.visitor.DefaultVisitor;
import ru.said.miami.orm.core.query.visitor.QueryElement;
import ru.said.miami.orm.core.query.visitor.QueryVisitor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static CreateTableQuery buildQuery(String typeName, List<DBFieldType> fieldTypes) {
        return new CreateTableQuery(
                typeName,
                fieldTypes.stream().map(AttributeDefenition::new).collect(Collectors.toList()),
                new DefaultVisitor()
        );
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
    public Boolean execute(Connection connection) throws SQLException {
        this.accept(visitor);
        String sql = visitor.getQuery();

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);

            return true;
        }
    }
}
