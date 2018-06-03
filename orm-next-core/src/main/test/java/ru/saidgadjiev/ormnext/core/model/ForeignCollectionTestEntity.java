package ru.saidgadjiev.ormnext.core.model;

import ru.saidgadjiev.ormnext.core.field.DatabaseColumn;
import ru.saidgadjiev.ormnext.core.field.ForeignCollectionField;
import ru.saidgadjiev.ormnext.core.field.ForeignColumn;

import java.util.ArrayList;
import java.util.List;

public class ForeignCollectionTestEntity {

    @DatabaseColumn(id = true, generated = true)
    private int id;

    @ForeignCollectionField
    private List<TestEntity> entities = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TestEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<TestEntity> entities) {
        this.entities = entities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignCollectionTestEntity that = (ForeignCollectionTestEntity) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}