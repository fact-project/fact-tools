package fact.rta;

import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import stream.io.SourceURL;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Random;

/**
 * Created by kai on 20.05.16.
 */
public class WebServiceTest {

    @Test
    public void startServer() throws SQLException, InterruptedException, IOException {
        URL resource = JDBITest.class.getResource("/data.sqlite");
        RTAWebService s = new RTAWebService();
        s.sqlitePath = new SourceURL(resource);
        DBI dbi = new DBI("jdbc:sqlite:" + s.sqlitePath.getPath());
        RTASignalTable t = dbi.open(RTASignalTable.class);
        t.createSignalTableIfNotExists();
        for (int i = 0; i < 3600; i++) {
            Thread.sleep(1100);
            Random random = new Random();

            double[] photons = random.doubles(1440).toArray();
            double thetaSquare = random.nextDouble();
            double size = random.nextDouble()*1000;
            double energy = random.nextDouble()*2000;
            DateTime now = DateTime.now();
            if (i % 30 == 0) {
                RTAProcessor.SignalContainer c = new RTAProcessor().new SignalContainer(random.nextInt(2), random.nextInt(5), 5, 0.4, 0.2);
                Range<DateTime> dateTimeRange = Range.closedOpen(now.minusSeconds(15), now.plusSeconds(15));
                s.updateLightCurve(dateTimeRange, c, "Wow");
            }



            s.updateEvent(photons, energy, size, thetaSquare, "Wow", DateTime.now());
            s.updateDataRate(now , new Random().nextDouble()*10);
        }

    }
}
