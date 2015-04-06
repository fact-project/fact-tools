package fact.services;

import fact.auxservice.AuxWebService;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by kai on 01.03.15.
 */
public class AuxWebServiceTest {

    @Test
    public void testTimeFlooring() throws IOException {
        DateTime time = new DateTime(1987, 9, 20, 12, 40, 34);
        DateTime roundedTime = AuxWebService.floorToQuarterHour(time);

        assertThat(roundedTime, is(new DateTime(1987, 9, 20, 12, 30, 00)));


        time = new DateTime(1987, 9, 20, 23, 59, 59);
        roundedTime = AuxWebService.floorToQuarterHour(time);

        assertThat(roundedTime, is(new DateTime(1987, 9, 20, 23, 45, 00)));


        time = new DateTime(1987, 9, 20, 00, 00, 01);
        roundedTime = AuxWebService.floorToQuarterHour(time);

        assertThat(roundedTime, is(new DateTime(1987, 9, 20, 00, 00, 00)));
    }
}
