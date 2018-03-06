package fact.gainservice;

import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class GainServiceTest {

    @Test
    public void testLoadGains () throws Exception {

        GainService gainService = new GainService();

        gainService.loadGains();

    }

    @Test
    public void testGetNearest () {
        GainService gainService = new GainService();

        gainService.gains = new TreeMap<>();
        gainService.gains.put(ZonedDateTime.of(2017, 1, 1, 22, 0, 0, 0, ZoneOffset.UTC), new double[]{1});
        gainService.gains.put(ZonedDateTime.of(2017, 1, 1, 22, 30, 0, 0, ZoneOffset.UTC), new double[]{2});

        double[] gains = gainService.getGains(ZonedDateTime.of(2017, 1, 1, 22, 10, 0, 0, ZoneOffset.UTC));
        assertEquals(1, gains[0], 1e-12);

        gains = gainService.getGains(ZonedDateTime.of(2017, 1, 1, 22, 20, 0, 0, ZoneOffset.UTC));
        assertEquals(2, gains[0], 1e-12);

    }

    @Test
    public void testLoadSimulationGain() {
        GainService gainService = new GainService();
        double[] gains = gainService.getSimulationGains();

        assertEquals(266.0, gains[0], 1e-10);
    }
}
