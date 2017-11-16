package fact.TriggerEmulation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by jbuss on 16.11.17.
 */
public class DiscriminatorTest {

    @Test
    public void testBooleanToInt(){
        EmulateDiscriminator emulateDiscriminator = new EmulateDiscriminator();
        Assert.assertEquals(emulateDiscriminator.booleanToInt(true), 1);
        Assert.assertEquals(emulateDiscriminator.booleanToInt(false), 0);
    }

    @Test
    public void testDiscriminator(){
        EmulateDiscriminator emulateDiscriminator = new EmulateDiscriminator();
        double[] data = new double[300];
        Arrays.fill(data, 200);
        for (int i = 20; i < 30; i++) {
            data[i] = 300.;
        }
        int estimated_slice = emulateDiscriminator.discriminatePatch(
                data,
                220,
                8,
                10,
                50
        );
        int true_slice = 20;
        Assert.assertTrue(estimated_slice == true_slice);

    }
}
