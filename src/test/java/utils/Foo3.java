package utils;

import field.DBField;
import field.DataType;
import field.ManyToMany;
import field.ManyToOne;
import table.DBTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 06.05.17.
 */

@DBTable(name = "foo3")
public class Foo3 {
    @DBField(id = true, autoGeneratedId = true, dataType = DataType.LONG, fieldName = "id")
    private long id;

    @DBField(dataType = DataType.STRING, fieldName = "test_name")
    private String name;

    @ManyToMany(mappedBy = "foo3List")
    List<Foo> fooList = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Foo> getFooList() {
        return fooList;
    }

    @Override
    public String toString() {
        return "utils.Foo2{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
