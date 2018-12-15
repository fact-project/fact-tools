package fact.pedestalSuperposition;

import fact.Constants;
import fact.gainservice.GainService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class rescaleDataArrayTest {

    private Random random = new Random();

    private double[] data;
    private GainService gainService = new GainService();

    private double[] mcGains = gainService.getSimulationGains();
    private double[] dataGains = gainService.getGains(ZonedDateTime.of(2017, 1, 1, 22, 20, 0, 0, ZoneOffset.UTC));

    @Before
    public void init(){
        data = random.doubles(Constants.N_PIXELS * 1, -10, 300).toArray();
    }

    @Test
    public void testIdentityMCRescaleDataArray() {

        double[] result = rescaleDataArray.rescaleDataArray(data, mcGains, mcGains, true,  1);
        Assert.assertArrayEquals(data, result, 0.005);
    }

    @Test
    public void testIdentityDataRescaleDataArray() {

        double[] result_mc = rescaleDataArray.rescaleDataArray(data, dataGains, dataGains, true,  1);
        Assert.assertArrayEquals(data, result_mc, 0.005);
    }

    @Test
    public void testMcConstantsRescaleDataArray() {

        double[] result = rescaleDataArray.rescaleDataArray(data, dataGains, mcGains,true,  1);

        double[] back_trafo = rescaleDataArray.rescaleDataArray(result, dataGains, mcGains,false,  1);

        Assert.assertArrayEquals(back_trafo, data, 0.5);
    }
}