package ru.said.orm.next.core.field.field_type;

import ru.said.orm.next.core.field.DataType;
import ru.said.orm.next.core.field.persisters.DataPersister;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by said on 30.10.17.
 */
public class ForeignFieldType implements IDBFieldType {

    private static final String ID_SUFFIX = "_id";

    private IDBFieldType dbFieldType;

    private IDBFieldType foreignPrimaryKey;

    private boolean foreignAutoCreate;

    private Class<?> foreignFieldClass;

    private DataPersister dataPersister;

    private String foreignTableName;

    private DataType dataType;

    public ForeignFieldType(IDBFieldType dbFieldType) {
        this.dbFieldType = dbFieldType;
    }

    @Override
    public boolean isId() {
        return dbFieldType.isId();
    }

    @Override
    public boolean isNotNull() {
        return dbFieldType.isNotNull();
    }

    @Override
    public boolean isGenerated() {
        return dbFieldType.isGenerated();
    }

    @Override
    public Field getField() {
        return dbFieldType.getField();
    }

    @Override
    public int getLength() {
        return dbFieldType.getLength();
    }

    public boolean isForeignAutoCreate() {
        return foreignAutoCreate;
    }

    public Class<?> getForeignFieldClass() {
        return foreignFieldClass;
    }

    @Override
    public DataPersister getDataPersister() {
        return dataPersister;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    public IDBFieldType getForeignPrimaryKey() {
        return foreignPrimaryKey;
    }

    @Override
    public Object access(Object object) throws InvocationTargetException, IllegalAccessException {
        return dbFieldType.access(object);
    }

    @Override
    public void assign(Object object, Object value) throws IllegalAccessException, InvocationTargetException {
        dbFieldType.assign(object, value);
    }

    @Override
    public String getColumnName() {
        return dbFieldType.getColumnName() + ID_SUFFIX;
    }

    public String getForeignTableName() {
        return foreignTableName;
    }

    public String getForeignColumnName() {
        return foreignPrimaryKey.getColumnName();
    }

    @Override
    public boolean isForeignFieldType() {
        return true;
    }

    public void setForeignPrimaryKey(IDBFieldType foreignPrimaryKey) {
        this.foreignPrimaryKey = foreignPrimaryKey;
    }

    public void setForeignAutoCreate(boolean foreignAutoCreate) {
        this.foreignAutoCreate = foreignAutoCreate;
    }

    public void setForeignFieldClass(Class<?> foreignFieldClass) {
        this.foreignFieldClass = foreignFieldClass;
    }

    public void setDataPersister(DataPersister dataPersister) {
        this.dataPersister = dataPersister;
    }

    public void setForeignTableName(String foreignTableName) {
        this.foreignTableName = foreignTableName;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
}