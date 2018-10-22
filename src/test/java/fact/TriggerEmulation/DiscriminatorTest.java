package fact.TriggerEmulation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static fact.TriggerEmulation.Discriminator.*;

/**
 * Tests the discriminator functions
 * Created by jbuss on 16.11.17.
 */
public class DiscriminatorTest {

    @Test
    public void testBooleanToInt(){
        Assert.assertEquals(Discriminator.booleanToInt(true), 1);
        Assert.assertEquals(Discriminator.booleanToInt(false), 0);
    }

    @Test
    public void testDiscriminator(){
        EmulateDiscriminator emulateDiscriminator = new EmulateDiscriminator();
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
        EmulateDiscriminator emulateDiscriminator = new EmulateDiscriminator();
        double[][] data = new double[3][300];
        Arrays.fill(data[0], 200);
        Arrays.fill(data[1], 200);
        Arrays.fill(data[2], 200);
        for (int i = 20; i < 30; i++) {
            data[1][i] = 300.;
        }

        DiscriminatorOutput[] discriminatorOutputs = discriminatePatches(
                data,
                millivoltToDAC(220),
                8,
                10,
                50
        );
        int default_slice =  Discriminator.default_slice;
        boolean[] expectedPrimitives = {false, true, false};
        int[] expectedSlices = {default_slice, 20, default_slice};
        Assert.assertArrayEquals(expectedPrimitives, discriminatorOutputsToTriggerPrimitiveArray(discriminatorOutputs));
        Assert.assertArrayEquals(expectedSlices, discriminatorOutputsToTriggerSliceArray(discriminatorOutputs));

    }
}
