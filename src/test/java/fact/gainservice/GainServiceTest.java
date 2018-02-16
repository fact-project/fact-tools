package fact.gainservice;

import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class GainServiceTest {

    @Test
    public void testGetGains () {

        GainService gainService = new GainService();

        gainService.getGains(ZonedDateTime.of(2016, 11, 10, 4, 0, 0, 0, ZoneOffset.UTC));

    }
}
