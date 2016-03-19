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
import java.sql.SQLException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test some aspects of jooq
 * Created by kai on 04.02.16.
 */
public class JooqTest {

    /**
     * Open the test sqlite database and print some information thats in there.
     * @throws SQLException
     */
    @Test
    public void  testJooq() throws SQLException {
        String url = "jdbc:sqlite:rta.sqlite";

        Connection conn = DriverManager.getConnection(url, "", "");
        DSLContext create = DSL.using(conn, SQLDialect.SQLITE);
        Result<Record> result = create.select().from(Sources.SOURCES).fetch();
        assertThat(result, is(not(nullValue())));

        for(Record a : result){
            System.out.println(a.getValue(Sources.SOURCES.SOURCE_NAME));
        }
    }
}
