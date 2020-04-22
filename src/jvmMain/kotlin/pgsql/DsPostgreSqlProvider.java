package pgsql;

/* User: Simone 30/03/13 9.32 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DsPostgreSqlProvider {

    public static final String HOST = "localhost";
    public static final String PORT = "1603";
    public static final String NAME = "db";
    public static final String USERNAME = "simonegiacomelli";
    public static final String PASSWORD = "usi";


    private String hostname;


    public void init(Connection pgConn) {
        try {

            Statement statement = pgConn.createStatement();
            if (dbNotExist(statement))
                statement.execute(String.format("CREATE DATABASE %s WITH OWNER = %s ENCODING = 'UTF8' " +
                                "TABLESPACE = pg_default LC_COLLATE = 'C' LC_CTYPE = 'C' CONNECTION LIMIT = -1;"
                        , NAME, USERNAME));
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean dbNotExist(Statement statement) throws SQLException {
        ResultSet dbCountRs = statement.executeQuery(String.format("select count(*) from pg_database where datname='%s'", NAME));
        dbCountRs.next();
        int dbCount = dbCountRs.getInt(1);
        dbCountRs.close();
        return dbCount == 0;
    }

//
//    private BasicDataSource getDataSource(String url, BasicDataSource ds) {
//        ds.setUrl(url);
//        ds.setDriverClassName("org.postgresql.Driver");
//        ds.setUsername(getUser());
//        ds.setPassword(getPassword());
//        ds.setMaxWait(1000);
//        ds.setMaxActive(16);
//        return ds;
//    }

}
