package fact.services;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxWebService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.SqliteService;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.auxservice.strategies.Closest;
import fact.auxservice.strategies.Later;
import fact.features.source.SourcePosition;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import stream.io.SourceURL;

import java.io.IOException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TreeSet;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test the implementation of the sqliteservice test by querying the test files.
 * Created by kai on 06.12.15.
 */
public class SqliteTest {

    @Test
    public void testSourcePosition() throws Exception {
        SqliteService s = new SqliteService();
        s.setUrl(new SourceURL(SqliteTest.class.getResource("/drive_control_unittest_20140118_19.sqlite")));

        OffsetDateTime t =  OffsetDateTime.parse("2014-01-19T01:40:33+00:00");

        TreeSet<AuxPoint> r = s.loadDataFromDataBase(AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION, t);

        assertThat(r, is(not(nullValue())));

        OffsetDateTime first  = r.first().getTimeStamp();
        OffsetDateTime last  = r.last().getTimeStamp();


        assertThat(first, is(OffsetDateTime.parse("2014-01-19T01:22:18.045+00:00")));
        assertThat(last, is(OffsetDateTime.parse("2014-01-19T01:42:58.391+00:00")));
        assertThat(r.size(), is(9));
    }

    @Test
    public void testSourcePositionInMay() throws Exception {
        SqliteService s = new SqliteService();
        s.setUrl(new SourceURL(SqliteTest.class.getResource("/drive_control_5_20.sqlite")));
        OffsetDateTime t =  OffsetDateTime.parse("2014-05-20T23:46:14.560Z");
        AuxPoint auxiliaryData = s.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION, t, new Closest());
        assertThat(auxiliaryData, is(not(nullValue())));
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
        s.setUrl(new SourceURL(SqliteTest.class.getResource("/drive_control_unittest_20140118_19.sqlite")));

        OffsetDateTime t =  OffsetDateTime.parse("2014-01-19T01:34:00.04+00:00");
        TreeSet<AuxPoint> r = s.loadDataFromDataBase(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, t);

        assertThat(r, is(not(nullValue())));

        OffsetDateTime first  = r.first().getTimeStamp();
        OffsetDateTime last  = r.last().getTimeStamp();

        assertThat(first, is(OffsetDateTime.parse("2014-01-19T01:33:17.164+00:00")));
        assertThat(last, is(OffsetDateTime.parse("2014-01-19T01:43:58.684+00:00")));
        assertThat(r.size(), is(480));
    }

    @Test
    public void testTimeFlooring() throws IOException {
        OffsetDateTime time = OffsetDateTime.of(1987, 9, 20, 12, 40, 34,0, ZoneOffset.of("+00:00"));

        SqliteService.AuxDataCacheKey key = new SqliteService().new AuxDataCacheKey(AuxiliaryServiceName.BIAS_CONTROL_DAC, time);

        OffsetDateTime roundedTime = key.floorToQuarterHour(time);
        assertThat(roundedTime, is(OffsetDateTime.of(1987, 9, 20, 12, 30, 00,0,ZoneOffset.of("+00:00"))));


        time = OffsetDateTime.of(1987, 9, 20, 23, 59, 59,0,ZoneOffset.of("+00:00"));
        roundedTime = AuxWebService.floorToQuarterHour(time);

        assertThat(roundedTime, is(OffsetDateTime.of(1987, 9, 20, 23, 45, 00,0,ZoneOffset.of("+00:00"))));


        time = OffsetDateTime.of(1987, 9, 20, 00, 00, 01,0,ZoneOffset.of("+00:00"));
        roundedTime = AuxWebService.floorToQuarterHour(time);

        assertThat(roundedTime, is(OffsetDateTime.of(1987, 9, 20, 00, 00, 00,0,ZoneOffset.of("+00:00"))));
    }


}
