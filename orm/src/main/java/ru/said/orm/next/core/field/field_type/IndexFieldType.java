package ru.said.orm.next.core.field.field_type;

import ru.said.orm.next.core.table.Index;
import ru.said.orm.next.core.table.utils.TableInfoUtils;

import java.util.ArrayList;
import java.util.List;

public class IndexFieldType {

    private List<String> columns;

    private String name;

    private boolean unique;

    private String tableName;

    public IndexFieldType(String name, boolean unique, String tableName, List<String> columns) {
        this.columns = columns;
        this.name = name;
        this.tableName = tableName;
        this.unique = unique;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isUnique() {
        return unique;
    }

}