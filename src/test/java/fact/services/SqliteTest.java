package fact.services;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.SqliteService;
import fact.features.source.SourcePosition;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

        DateTime t =  DateTime.parse("2014-01-19T01:40:33+00:00");
        TreeSet<AuxPoint> r = s.loadDataFromDataBase(AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION, t);

        assertThat(r, is(not(nullValue())));

        DateTime first  = r.first().getTimeStamp();
        DateTime last  = r.last().getTimeStamp();

        assertThat(first, is(DateTime.parse("2014-01-19T01:22:18.045").withZone(DateTimeZone.UTC)));
        assertThat(last, is(DateTime.parse("2014-01-19T01:42:58.391").withZone(DateTimeZone.UTC)));
        assertThat(r.size(), is(9));
    }

    /**
     * Should deliver the same result as
     *       SELECT * FROM DRIVE_CONTROL_TRACKING_POSITION WHERE Time BETWEEN "2014-01-19 01:33:00" AND "2014-01-19 01:44:00"
     *  on the test DB
     * @throws Exception
     */
    @Test
    public void testTrackingPosition() throws Exception {
        SqliteService s = new SqliteService();
        s.setUrl(new SourceURL(SqliteTest.class.getResource("/drive_control.sqlite")));

        DateTime t =  DateTime.parse("2014-01-19T01:34:00+00:00");
        TreeSet<AuxPoint> r = s.loadDataFromDataBase(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, t);
        assertThat(r, is(not(nullValue())));

        DateTime first  = r.first().getTimeStamp();
        DateTime last  = r.last().getTimeStamp();

        assertThat(first, is(DateTime.parse("2014-01-19T01:33:17.164").withZone(DateTimeZone.UTC)));
        assertThat(last, is(DateTime.parse("2014-01-19T01:43:58.684").withZone(DateTimeZone.UTC)));
        assertThat(r.size(), is(480));
    }

}
