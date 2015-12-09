package fact.services;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.SqliteService;
import org.joda.time.DateTime;
import org.junit.Test;
import stream.io.SourceURL;

import java.util.TreeSet;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by kai on 06.12.15.
 */
public class SqliteTest {

    @Test
    public void testSqLiteService() throws Exception {
        SqliteService s = new SqliteService();
        s.setUrl(new SourceURL("file:////Users/kai/Development/ma/pointing.sqlite"));
        DateTime t =  DateTime.parse("2014-01-18T22:11:59+00:00");

        System.out.println(t);
        TreeSet<AuxPoint> r = s.loadDataFromDataBase(AuxiliaryServiceName.DRIVE_CONTROL_POINTING_POSITION, t);
        assertThat(r, is(not(nullValue())));
    }

}
