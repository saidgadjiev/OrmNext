package ru.said.miami.orm.core.cache.core.clause;

import ru.said.miami.orm.core.cache.core.dao.Dao;
import ru.said.miami.orm.core.cache.core.dao.BaseDaoImpl;
import org.junit.Assert;
import org.junit.Test;
import test_table.Foo;

import static org.mockito.Mockito.mock;

/**
 * Created by said on 07.05.17.
 */
public class QueryBuilderTest {

    @Test
    public void limit() throws Exception {
        QueryBuilder<Foo> queryBuilder = new QueryBuilder<>(Foo.class, mock(Dao.class));

        queryBuilder.limit(1);
        Assert.assertEquals("SELECT id FROM foo LIMIT 1", queryBuilder.getStringQuery());
    }

    @Test
    public void orderBy() throws Exception {
        QueryBuilder<Foo> queryBuilder = new QueryBuilder<>(Foo.class, mock(Dao.class));

        queryBuilder.orderBy(QueryBuilder.ORDER_BY.ASC);
        Assert.assertEquals("SELECT id FROM foo ORDER BY ASC", queryBuilder.getStringQuery());
    }

    @Test
    public void where() throws Exception {
        QueryBuilder<Foo> queryBuilder = new QueryBuilder<>(Foo.class, mock(Dao.class));

        Where where = new Where();

        where.addEqClause("name", "test_foo");
        queryBuilder.where(where);
        Assert.assertEquals("SELECT id FROM foo WHERE name='test_foo'", queryBuilder.getStringQuery());
    }

    @Test
    public void executeQuery() throws Exception {

    }

    @Test
    public void getStringQuery() throws Exception {
        QueryBuilder<Foo> queryBuilder = new QueryBuilder<>(Foo.class, mock(Dao.class));
        Where where = new Where();

        where.addEqClause("name", "test_foo");
        queryBuilder.where(where);
        queryBuilder.orderBy(QueryBuilder.ORDER_BY.ASC);
        queryBuilder.limit(1);

        Assert.assertEquals("SELECT id FROM foo WHERE name='test_foo' ORDER BY ASC LIMIT 1", queryBuilder.getStringQuery());

    }

    private Dao<Foo> createDao(ConnectionSource connectionSource) {
        return new BaseDaoImpl<>(connectionSource, Foo.class);
    }

}