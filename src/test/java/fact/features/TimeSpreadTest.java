package fact.features;

import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import java.util.Random;

import static org.junit.Assert.*;

public class TimeSpreadTest {

    @Test
    public void testNoPixels() {
        PixelSet pixelSet = new PixelSet();
        assertEquals(0, pixelSet.size());

        double[] weights = new double[1440];
        double[] times = new double[1440];

        Data item = DataFactory.create();
        item.put("weights", weights);
        item.put("times", times);
        item.put("shower", pixelSet);


        TimeSpread timeSpread = new TimeSpread();
        timeSpread.arrivalTimeKey = "times";
        timeSpread.pixelSetKey = "shower";
        timeSpread.weightsKey = "weights";
        timeSpread.outputKey = "timespread";
        timeSpread.process(item);

        assertTrue(Double.isNaN((Double) item.get("timespread")));
        assertTrue(Double.isNaN((Double) item.get("timespread_weighted")));
    }

    @Test
    public void testEqualValues() {
        PixelSet pixelSet = PixelSet.fromCHIDs(new int[] {1, 2, 3, 4, 5});

        double[] weights = new double[1440];
        double[] times = new double[1440];
        for (CameraPixel pixel: pixelSet) {
            weights[pixel.id] = 1.0;
            times[pixel.id] = 1.0;
        }

        Data item = DataFactory.create();
        item.put("weights", weights);
        item.put("times", times);
        item.put("shower", pixelSet);


        TimeSpread timeSpread = new TimeSpread();
        timeSpread.arrivalTimeKey = "times";
        timeSpread.pixelSetKey = "shower";
        timeSpread.weightsKey = "weights";
        timeSpread.outputKey = "timespread";
        timeSpread.process(item);

        assertEquals(0.0, (double) item.get("timespread"), 1e-6);
        assertEquals(0.0, (double) item.get("timespread_weighted"), 1e-6);
    }

    @Test
    public void testTimeSpread() {
        PixelSet pixelSet = PixelSet.fromCHIDs(new int[] {1, 2, 3, 4, 5});

        Random random = new Random(0);
        double[] weights = new double[1440];
        double[] times = new double[1440];
        for (CameraPixel pixel: pixelSet) {
            weights[pixel.id] = random.nextDouble() * 5;
            times[pixel.id] = random.nextGaussian() * 10 + 50;
        }

        Data item = DataFactory.create();
        item.put("weights", weights);
        item.put("times", times);
        item.put("shower", pixelSet);


        TimeSpread timeSpread = new TimeSpread();
        timeSpread.arrivalTimeKey = "times";
        timeSpread.pixelSetKey = "shower";
        timeSpread.weightsKey = "weights";
        timeSpread.outputKey = "timespread";
        timeSpread.process(item);

        assertFalse(Double.isNaN((Double) item.get("timespread")));
        assertFalse(Double.isNaN((Double) item.get("timespread_weighted")));
    }
}
