package fact.TriggerEmulation;

import fact.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static fact.TriggerEmulation.Discriminator.*;

/**
 * Tests the discriminator functions
 * Created by jbuss on 16.11.17.
 */
public class DiscriminatorTest {
    private DiscriminatorOutput[] discriminatorOutputs;
    private double[][] data;
    private int default_slice =  Discriminator.default_slice;

    @Before
    public void initialize() {
        data = new double[3][300];
        Arrays.fill(data[0], 200);
        Arrays.fill(data[1], 200);
        Arrays.fill(data[2], 200);
        for (int i = 20; i < 30; i++) {
            data[1][i] = 300.;
        }

        discriminatorOutputs = discriminatePatches(
                data,
                millivoltToDAC(220),
                8,
                10,
                50
        );
    }

    @Test
    public void testBooleanToInt(){
        Assert.assertEquals(Discriminator.booleanToInt(true), 1);
        Assert.assertEquals(Discriminator.booleanToInt(false), 0);
    }

    @Test
    public void testDiscriminator(){
        double[] data = new double[300];
        Arrays.fill(data, 200);
        for (int i = 20; i < 30; i++) {
            data[i] = 300.;
        }
        DiscriminatorOutput discriminatorOutputs = Discriminator.discriminatePatch(
                data,
                millivoltToDAC(220),
                8,
                10,
                50
        );
        int true_slice = 20;
        Assert.assertTrue(discriminatorOutputs.triggerSlice == true_slice);

    }
    @Test
    public void testDiscriminatePatches(){

        boolean[] expectedPrimitives = {false, true, false};
        int[] expectedSlices = {default_slice, 20, default_slice};
        Assert.assertArrayEquals(expectedPrimitives, discriminatorOutputsToTriggerPrimitiveArray(discriminatorOutputs));
        Assert.assertArrayEquals(expectedSlices, discriminatorOutputsToTriggerSliceArray(discriminatorOutputs));

    }

    @Test
    public void testDacToMillivolt() {
        Assert.assertTrue(0.61 == dacToMillivolt(1));
    }

    @Test
    public void testMillivoltToDAC() {
        Assert.assertTrue(1 == millivoltToDAC(0.61));
    }

    @Test
    public void testDiscriminatorOutputsToTriggerSliceArray() {
        boolean[] result = discriminatorOutputsToTriggerPrimitiveArray(discriminatorOutputs);
        boolean[] expected = {false, true, false};

        Assert.assertArrayEquals(expected, result);

    }

    @Test
    public void testDiscriminatorOutputsToTriggerPrimitiveArray() {
        int[] result = discriminatorOutputsToTriggerSliceArray(discriminatorOutputs);
        int[] expected = {0, 20, 0};

        Assert.assertArrayEquals(expected, result);
    }
}
