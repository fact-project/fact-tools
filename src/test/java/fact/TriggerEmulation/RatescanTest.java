package fact.TriggerEmulation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import fact.TriggerEmulation.Ratescan.*;

import static fact.TriggerEmulation.Discriminator.dacToMillivolt;

public class RatescanTest {
    private Ratescan ratescan;
    private double[][] data;
    private double baseline = dacToMillivolt(100);

    @Before
    public void initialize() {
        ratescan = new Ratescan();
        data = new double[3][300];
        Arrays.fill(data[0], baseline);
        Arrays.fill(data[1], baseline);
        Arrays.fill(data[2], baseline);
        for (int i = 50; i < 60; i++) {
            data[0][i] = dacToMillivolt(250);
            data[1][i] = dacToMillivolt(300);
            data[2][i] = dacToMillivolt(350);
        }

    }

    public void assertions(RatescanResult result) {
        Assert.assertEquals(9, result.thresholdsArray.size());

        Assert.assertEquals(400, result.getThresholds()[8]);
        Assert.assertEquals(350, result.getThresholds()[7]);
        Assert.assertEquals(300, result.getThresholds()[6]);
        Assert.assertEquals(250, result.getThresholds()[5]);

        boolean[] expected_at_400_DAC = {false,false,false};
        boolean[] expected_at_350_DAC = {false,false,true,};
        boolean[] expected_at_300_DAC = {false,true,true};
        boolean[] expected_at_250_DAC = {true,true,true};

        Assert.assertArrayEquals(expected_at_400_DAC, result.triggerPrimitivesArray.get(8));
        Assert.assertArrayEquals(expected_at_350_DAC, result.triggerPrimitivesArray.get(7));
        Assert.assertArrayEquals(expected_at_300_DAC, result.triggerPrimitivesArray.get(6));
        Assert.assertArrayEquals(expected_at_250_DAC, result.triggerPrimitivesArray.get(5));

        Assert.assertEquals(0, result.getNumberOfPrimitives()[8]);
        Assert.assertEquals(1, result.getNumberOfPrimitives()[7]);
        Assert.assertEquals(2, result.getNumberOfPrimitives()[6]);
        Assert.assertEquals(3, result.getNumberOfPrimitives()[5]);

        int[] expected_slice_at_400_DAC = {0, 0, 0};
        int[] expected_slice_at_350_DAC = {0, 0, 50};
        int[] expected_slice_at_300_DAC = {0, 50, 50};
        int[] expected_slice_at_250_DAC = {50, 50, 50};

        Assert.assertArrayEquals(expected_slice_at_400_DAC, result.getTriggerSlices()[8]);
        Assert.assertArrayEquals(expected_slice_at_350_DAC, result.getTriggerSlices()[7]);
        Assert.assertArrayEquals(expected_slice_at_300_DAC, result.getTriggerSlices()[6]);
        Assert.assertArrayEquals(expected_slice_at_250_DAC, result.getTriggerSlices()[5]);
    }

    @Test
    public void ratescan() {
        RatescanResult result =ratescan.ratescan(data, 0, 50, 9);

        assertions(result);
    }

    @Test
    public void ratescan_endless() {
        RatescanResult result =ratescan.ratescan(data, 0, 50);

        assertions(result);
    }

    @Test
    public void countPrimitives() {
        boolean[] primitives = {true, false, true, false};

        int n_primitives = Ratescan.countPrimitives(primitives);
        Assert.assertEquals(2, n_primitives);
    }
}