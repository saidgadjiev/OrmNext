package ru.said.miami.orm.core;

import ru.said.miami.orm.core.field.DBField;
import ru.said.miami.orm.core.field.DataType;
import ru.said.miami.orm.core.table.DBTable;

@DBTable(name = "order")
public class Order {

    @DBField(columnName = "id", dataType = DataType.INTEGER, id = true, generated = true)
    private Integer id;

    @DBField
    private String name;

    @DBField(foreign = true)
    private Account account;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}