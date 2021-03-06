package ru.saidgadjiev.ormnext.core.dao;

import org.junit.Assert;
import org.junit.Test;
import ru.saidgadjiev.ormnext.core.BaseCoreTest;
import ru.saidgadjiev.ormnext.core.field.DatabaseColumn;
import ru.saidgadjiev.ormnext.core.field.ForeignCollectionField;
import ru.saidgadjiev.ormnext.core.field.ForeignColumn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 07.08.2018.
 */
public class TestRefresh extends BaseCoreTest {

    @Test
    public void test() throws SQLException {
        try (Session session = createSessionAndCreateTables(AForeignCollection.class, A.class)) {
            AForeignCollection foreignTestEntity = new AForeignCollection();

            session.create(foreignTestEntity);
            A a = new A();

            a.setaForeignCollection(foreignTestEntity);
            foreignTestEntity.getAs().add(a);
            session.create(a);
            AForeignCollection resultBefore = session.queryForId(AForeignCollection.class, 1);

            Assert.assertEquals(foreignTestEntity.getAs(), resultBefore.getAs());

            a.setDesc("Test");
            session.update(a);
            session.refresh(resultBefore);
            Assert.assertEquals(resultBefore.getAs(), foreignTestEntity.getAs());
        }
    }

    public static class A {

        @DatabaseColumn(id = true, generated = true)
        private int id;

        @DatabaseColumn
        private String desc;

        @ForeignColumn
        private AForeignCollection aForeignCollection;

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

        public AForeignCollection getaForeignCollection() {
            return aForeignCollection;
        }

        public void setaForeignCollection(AForeignCollection aForeignCollection) {
            this.aForeignCollection = aForeignCollection;
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

    public static class AForeignCollection {

        @DatabaseColumn(id = true, generated = true)
        private int id;

        @DatabaseColumn
        private String desc;

        @ForeignCollectionField
        private List<A> as = new ArrayList<>();

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setAs(List<A> as) {
            this.as = as;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public List<A> getAs() {
            return as;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AForeignCollection that = (AForeignCollection) o;

            if (id != that.id) return false;
            return desc != null ? desc.equals(that.desc) : that.desc == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (desc != null ? desc.hashCode() : 0);
            return result;
        }
    }
}
