package fact.rta;

import org.joda.time.DateTime;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;


/**
 * Test some aspects of JDBI
 * Created by kai on 24.05.16.
 */
public class JDBITest {

    @Test
    public void testInsert(){
        String db = JDBITest.class.getResource("/data.sqlite").getPath();
        System.out.println(db);
        DBI dbi = new DBI("jdbc:sqlite:" + db);
        Handle h = dbi.open();

        RTASignalTable t = dbi.open(RTASignalTable.class);
        t.dropTable();
        t.createSignalTableIfNotExists();
        DateTime now = DateTime.now();
        t.insert(now.toString(), now.plusMinutes(5).toString(), "Crab", 12, 3, 0.5, 04);
        t.insert(now.plusMinutes(5).toString(), now.plusMinutes(10).toString(), "Crab", 512, 13242, 0.5, 04);
        t.insert(now.plusMinutes(10).toString(), now.plusMinutes(15).toString(), "Crab", 13432, 12, 0.5, 04);

        //print all entries
        h.createQuery("SELECT * FROM RTASignal").forEach((obj) -> System.out.println(String.valueOf(obj)));

        //insertt same prim key again
        t.insert(now.plusMinutes(5).toString(), now.plusMinutes(10).toString(), "Crab", 512, 13242, 0.5, 04);

        h.close();
    }
}
