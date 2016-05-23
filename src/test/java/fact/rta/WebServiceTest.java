package fact.rta;

import org.joda.time.DateTime;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Random;

/**
 * Created by kai on 20.05.16.
 */
public class WebServiceTest {

    @Test
    public void startServer() throws SQLException, InterruptedException {
        RTAWebService s = new RTAWebService();
        for (int i = 0; i < 3600; i++) {
            Thread.sleep(1100);
            Random random = new Random();

            double[] photons = random.doubles(1440).toArray();
            double thetaSquare = random.nextDouble();
            double size = random.nextDouble()*1000;
            double energy = random.nextDouble()*2000;


            s.updateEvent(photons, energy, size, thetaSquare, "Wow", DateTime.now());
            s.updateDataRate(DateTime.now() , new Random().nextDouble()*10);
        }

    }
}
