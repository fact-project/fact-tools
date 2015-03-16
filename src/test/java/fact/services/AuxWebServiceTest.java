package fact.services;

import fact.auxservice.AuxWebService;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.fail;

/**
 * Created by kai on 01.03.15.
 */
public class AuxWebServiceTest {

    @Test
    public void testServicesConnection() throws IOException {
        AuxWebService web = new AuxWebService();
        web.getAuxiliaryData("TNG_WEATHER_DUST", new Date(123), new Date(123213));
        web.getAuxiliaryData("TNG_WEATHER_DATA", new Date(123), new Date(123213));
        web.getAuxiliaryData("LID_CONTROL_DATA", new Date(123), new Date(123213));
    }

    @Test
    public void testServiceDoesNotExist() throws IOException {
        AuxWebService web = new AuxWebService();
        try {
            web.getAuxiliaryData("SOME_WEIRD_SERVICE", new Date(123), new Date(123213));
        } catch (AuxWebService.ServiceDoesNotExistException e){
            return;
        }
        fail("This should have thrown an exception");
    }
}
