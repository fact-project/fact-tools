package fact.rta;

import fact.rta.persistence.tables.Sources;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by kai on 04.02.16.
 */
public class Jooq {

    @Test
    public void  testJooq(){
        String userName = "";
        String password = "";
        String url = "jdbc:sqlite:rta.sqlite";

        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        try (Connection conn = DriverManager.getConnection(url, userName, password)) {
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);
            Result<Record> result = create.select().from(Sources.SOURCES).fetch();
            System.out.println(result);
        }

        // For the sake of this tutorial, let's keep exception handling simple
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
