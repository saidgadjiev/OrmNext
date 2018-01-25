package ru.said.orm.next.core.table;

import ru.said.orm.next.core.field.field_type.*;
import ru.said.orm.next.core.table.utils.TableInfoUtils;
import ru.said.orm.next.core.table.validators.ForeignKeyValidator;
import ru.said.orm.next.core.table.validators.HasConstructorValidator;
import ru.said.orm.next.core.table.validators.IValidator;
import ru.said.orm.next.core.table.validators.PrimaryKeyValidator;
import ru.said.up.cache.core.Cache;
import ru.said.up.cache.core.CacheBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TableInfo<T> {

    private Class<T> tableClass;

    private List<DBFieldType> dbFieldTypes;

    private List<ForeignFieldType> foreignFieldTypes;

    private List<ForeignCollectionFieldType> foreignCollectionFieldTypes;

    private List<UniqueFieldType> uniqueFieldTypes;

    private List<IndexFieldType> indexFieldTypes;

    private IDBFieldType primaryKeyFieldType;

    private String tableName;

    private Constructor<T> constructor;

    private static List<IValidator> validators = new ArrayList<IValidator>() {{
        add(new ForeignKeyValidator());
        add(new HasConstructorValidator());
        add(new PrimaryKeyValidator());
    }};

    private TableInfo(Class<T> tableClass,
                      Constructor<T> constructor,
                      List<UniqueFieldType> uniqueFieldTypes,
                      List<IndexFieldType> indexFieldTypes,
                      String tableName,
                      List<IDBFieldType> fieldTypes) {
        this.tableClass = tableClass;
        this.tableName = tableName;
        this.constructor = constructor;
        this.indexFieldTypes = indexFieldTypes;
        this.primaryKeyFieldType = fieldTypes
                .stream()
                .filter(IDBFieldType::isId)
                .findAny()
                .orElse(null);
        this.uniqueFieldTypes = uniqueFieldTypes;
        this.dbFieldTypes = fieldTypes
                .stream()
                .filter(IDBFieldType::isDbFieldType)
                .map(idbFieldType -> (DBFieldType) idbFieldType)
                .collect(Collectors.toList());
        this.foreignFieldTypes = fieldTypes
                .stream()
                .filter(IDBFieldType::isForeignFieldType)
                .map(idbFieldType -> (ForeignFieldType) idbFieldType)
                .collect(Collectors.toList());
        this.foreignCollectionFieldTypes = fieldTypes
                .stream()
                .filter(IDBFieldType::isForeignCollectionFieldType)
                .map(idbFieldType -> (ForeignCollectionFieldType) idbFieldType)
                .collect(Collectors.toList());
    }

    public Class<T> getTableClass() {
        return tableClass;
    }

    public String getTableName() {
        return tableName;
    }

    public Optional<IDBFieldType> getPrimaryKeys() {
        return Optional.ofNullable(primaryKeyFieldType);
    }

    public List<UniqueFieldType> getUniqueFieldTypes() {
        return uniqueFieldTypes;
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

    public List<DBFieldType> toDBFieldTypes() {
        return Collections.unmodifiableList(dbFieldTypes);
    }

    public List<ForeignFieldType> toForeignFieldTypes() {
        return Collections.unmodifiableList(foreignFieldTypes);
    }

    public List<ForeignCollectionFieldType> toForeignCollectionFieldTypes() {
        return Collections.unmodifiableList(foreignCollectionFieldTypes);
    }

    public List<IndexFieldType> getIndexFieldTypes() {
        return indexFieldTypes;
    }

    public static <T> TableInfo<T> build(Class<T> clazz) throws Exception {
        for (IValidator validator : validators) {
            validator.validate(clazz);
        }
        List<IDBFieldType> fieldTypes = new ArrayList<>();
        //Не нравится этот код
        for (Field field : clazz.getDeclaredFields()) {
            DBFieldTypeFactory.create(field).ifPresent(fieldTypes::add);
        }

        return new TableInfo<>(
                clazz,
                (Constructor<T>) lookupDefaultConstructor(clazz).get(),
                resolveUniques(clazz),
                resolveIndexes(clazz),
                TableInfoUtils.resolveTableName(clazz),
                fieldTypes
        );
    }

    private static <T> List<UniqueFieldType> resolveUniques(Class<T> tClass) throws NoSuchFieldException, NoSuchMethodException {
        List<UniqueFieldType> uniqueFieldTypes = new ArrayList<>();
        if (tClass.isAnnotationPresent(DBTable.class)) {
            Unique[] uniques = tClass.getAnnotation(DBTable.class).uniqueConstraints();

            for (Unique unique : uniques) {
                uniqueFieldTypes.add(UniqueFieldType.build(unique, tClass));
            }
        }

        return uniqueFieldTypes;
    }

    private static <T> List<IndexFieldType> resolveIndexes(Class<T> tClass) throws NoSuchFieldException, NoSuchMethodException {
        List<IndexFieldType> uniqueFieldTypes = new ArrayList<>();
        if (tClass.isAnnotationPresent(DBTable.class)) {
            Index[] indexes = tClass.getAnnotation(DBTable.class).indexes();

            for (Index index : indexes) {
                uniqueFieldTypes.add(IndexFieldType.build(index, tClass));
            }
        }

        return uniqueFieldTypes;
    }

    public Optional<DBFieldType> getDBFieldTypeByFieldName(String name) {
        return dbFieldTypes.stream().filter(dbFieldType -> dbFieldType.getField().getName().equals(name)).findFirst();
    }

    public Optional<ForeignFieldType> getForeignFieldTypeByFieldName(String name) {
        return foreignFieldTypes.stream().filter(foreignFieldType -> foreignFieldType.getField().getName().equals(name)).findFirst();
    }

    public Optional<ForeignCollectionFieldType> getForeignCollectionFieldTypeByFieldName(String name) {
        return foreignCollectionFieldTypes.stream().filter(foreignCollectionFieldType -> foreignCollectionFieldType.getField().getName().equals(name)).findFirst();
    }

    private static Optional<Constructor<?>> lookupDefaultConstructor(Class<?> clazz) throws NoSuchMethodException {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return Optional.of(constructor);
            }
        }

        return Optional.empty();
    }

    public static class TableInfoCache {

        private static final Cache<Class<?>, TableInfo<?>> CACHE = CacheBuilder.newRefenceCacheBuilder().build();

        public static <T> TableInfo<T> build(Class<T> clazz) throws Exception {
            if (CACHE.contains(clazz)) {
                return (TableInfo<T>) CACHE.get(clazz);
            }
            TableInfo<?> tableInfo = TableInfo.build(clazz);

            CACHE.put(clazz, tableInfo);

            return (TableInfo<T>) tableInfo;
        }
    }
}


