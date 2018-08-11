package ru.saidgadjiev.ormnext.core.dao;

import org.junit.Assert;
import org.junit.Test;
import ru.saidgadjiev.ormnext.core.BaseCoreTest;
import ru.saidgadjiev.ormnext.core.field.DatabaseColumn;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.Criteria;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.DeleteStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.Restrictions;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.UpdateStatement;

import java.sql.SQLException;

/**
 * Created by said on 06.08.2018.
 */
public class TestUpdateStatement extends BaseCoreTest {

    @Test
    public void test() throws SQLException {
        try (Session session = createSessionAndCreateTables(A.class)) {
            A a = new A();

            a.setDesc("Test");

            session.create(a);

            UpdateStatement updateStatement = session.statementBuilder().createUpdateStatement(A.class);

            updateStatement
                    .set("desc", "Test1")
                    .where(new Criteria().add(Restrictions.eq("id", 1)));
            Assert.assertEquals(updateStatement.update(), 1);
            Assert.assertEquals("Test1", session.queryForId(A.class, 1).getDesc());
        }
    }

    public static class A {

        @DatabaseColumn(id = true, generated = true)
        private int id;

        @DatabaseColumn
        private String desc;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            A a = (A) o;

            if (id != a.id) return false;
            return desc != null ? desc.equals(a.desc) : a.desc == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (desc != null ? desc.hashCode() : 0);
            return result;
        }
    }
}
