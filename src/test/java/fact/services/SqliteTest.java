package fact.services;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.SqliteService;
import fact.features.source.SourcePosition;
import org.joda.time.DateTime;
import org.junit.Test;
import stream.io.SourceURL;

import java.net.URL;
import java.util.TreeSet;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test the implementation of the sqliteservice test by querying the test file.
 * Created by kai on 06.12.15.
 */
public class SqliteTest {

    @Test
    public void testSourcePosition() throws Exception {
        SqliteService s = new SqliteService();
        s.setUrl(new SourceURL(SqliteTest.class.getResource("/drive_control.sqlite")));

        DateTime t =  DateTime.parse("2014-01-19T01:31:33+00:00");
        TreeSet<AuxPoint> r = s.loadDataFromDataBase(AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION, t);

        assertThat(r, is(not(nullValue())));
        assertThat(r.size(), is(not(0)));
    }

    @Test
    public void testTrackingPosition() throws Exception {
        SqliteService s = new SqliteService();
        s.setUrl(new SourceURL(SqliteTest.class.getResource("/drive_control.sqlite")));

        DateTime t =  DateTime.parse("2014-01-19T01:31:33+00:00");
        TreeSet<AuxPoint> r = s.loadDataFromDataBase(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, t);

        assertThat(r, is(not(nullValue())));
        assertThat(r.size(), is(not(0)));
    }

}
