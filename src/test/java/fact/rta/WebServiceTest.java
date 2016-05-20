package fact.rta;

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
            Thread.sleep(500);
            s.updateDatarate(new Random().nextDouble()*10);
        }

    }
}
