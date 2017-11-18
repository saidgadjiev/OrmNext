package ru.said.miami.orm.core.dao;

import ru.said.miami.orm.core.cache.ObjectCache;
import ru.said.miami.orm.core.queryBuilder.PreparedQuery;
import ru.said.miami.orm.core.queryBuilder.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Класс для DAO
 * @param <T> тип объекта
 * @param <ID> тип id
 */
public interface Dao<T, ID> {

    /**
     * Метод сохраняет объект в базе
     * @param object создаваемый объект
     * @return количество созданных объектов. В данном случае 1
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    int create(T object) throws SQLException;

    boolean createTable(boolean ifNotExists) throws SQLException;

    /**
     * Метод получает объект по id
     * @param id целевой id объекта
     * @return возвращает объект с заданной id или null
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    T queryForId(ID id) throws SQLException;

    /**
     * Метод получает все объекты из таблицы T
     * @return все записи из таблицы типа T
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    List<T> queryForAll() throws SQLException;

    /**
     * Метод обновляет объект в базе
     * @param object обновляемый объект
     * @return количество обновленных объектов. В данном случае 1
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    int update(T object) throws SQLException;

    /**
     * Метод удаляет запись из базы
     * @param object удаляемый объект
     * @return количество удаленных объектов. В данном случае 1
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    int delete(T object) throws SQLException;

    int deleteById(ID id) throws SQLException;

    QueryBuilder<T> queryBuilder();

    void caching(boolean flag, ObjectCache objectCache);

    boolean dropTable(boolean ifExists) throws SQLException;

    void createIndexes() throws SQLException;

    void dropIndexes() throws SQLException;

    List<T> query(PreparedQuery preparedQuery) throws SQLException;
}
