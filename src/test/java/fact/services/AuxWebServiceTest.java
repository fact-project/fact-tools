package fact.services;

import fact.auxservice.AuxWebService;
import org.junit.Test;

import java.io.IOException;
import java.time.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * Created by kai on 01.03.15.
 */
public class AuxWebServiceTest {

    @Test
    public void testTimeFlooring() throws IOException {
        OffsetDateTime time = OffsetDateTime.of(1987, 9, 20, 12, 40, 34,0, ZoneOffset.of("+00:00"));
        OffsetDateTime roundedTime = AuxWebService.floorToQuarterHour(time);

        assertThat(roundedTime, is(OffsetDateTime.of(1987, 9, 20, 12, 30, 00,0,ZoneOffset.of("+00:00"))));


        time = OffsetDateTime.of(1987, 9, 20, 23, 59, 59,0,ZoneOffset.of("+00:00"));
        roundedTime = AuxWebService.floorToQuarterHour(time);

        assertThat(roundedTime, is(OffsetDateTime.of(1987, 9, 20, 23, 45, 00,0,ZoneOffset.of("+00:00"))));


        time = OffsetDateTime.of(1987, 9, 20, 00, 00, 01,0,ZoneOffset.of("+00:00"));
        roundedTime = AuxWebService.floorToQuarterHour(time);

        assertThat(roundedTime, is(OffsetDateTime.of(1987, 9, 20, 00, 00, 00,0,ZoneOffset.of("+00:00"))));
    }

}
