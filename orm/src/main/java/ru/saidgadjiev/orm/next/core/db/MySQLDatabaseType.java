package ru.saidgadjiev.orm.next.core.db;

/**
 * Created by said on 11.03.2018.
 */
public class MySQLDatabaseType implements DatabaseType {
    @Override
    public String appendPrimaryKey(boolean generated) {
        StringBuilder builder = new StringBuilder();

        if (generated) {
            builder.append(" AUTO_INCREMENT");
        }
        builder.append(" PRIMARY KEY");

        return builder.toString();
    }

    @Override
    public String getDatabaseName() {
        return "MySQL";
    }

    @Override
    public String getDriverClassName() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public String appendNoColumn() {
        return "() VALUES ()";
    }
}