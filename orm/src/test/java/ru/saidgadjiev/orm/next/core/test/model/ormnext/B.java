package ru.saidgadjiev.orm.next.core.test.model.ormnext;

import ru.saidgadjiev.orm.next.core.field.DatabaseColumn;
import ru.saidgadjiev.orm.next.core.field.ForeignCollectionField;
import ru.saidgadjiev.orm.next.core.field.ForeignColumn;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "b")
public class B {

    @DatabaseColumn(id = true, generated = true, dataType = 8)
    private int id;

    @DatabaseColumn(notNull = true)
    private String name;

    @ForeignColumn
    private C c;

    @ForeignColumn
    private A a;

    @ForeignCollectionField
    private Set<D> dSet = new HashSet<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<D> getdSet() {
        return dSet;
    }

    public void setdSet(Set<D> dSet) {
        this.dSet = dSet;
    }

    public C getC() {
        return c;
    }

    public void setC(C c) {
        this.c = c;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return "B{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", c=" + c +
                ", a=" + a +
                ", dSet=" + dSet +
                '}';
    }
}
