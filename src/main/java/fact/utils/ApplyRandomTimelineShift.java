package fact.utils;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Random;

/**
 * shift each pixel's timeline by a random offsets. The offset is sampled from a gaussian distribution around zero
 * with a given standard deviation.
 * <p>
 * Created by jbuss on 30.10.14.
 */
public class ApplyRandomTimelineShift implements Processor {
    static Logger log = LoggerFactory.getLogger(ApplyRandomTimelineShift.class);

    @Parameter(required = true, description = "key of the data array")
    public String key = null;

    @Parameter(description = "standard deviation of the random distribution")
    public double stdDeviation = 1.22;

    @Parameter(description = "Seed of the random number generator")
    public long Seed = 5901;

    @Parameter(required = true, description = "key of the output data array")
    public String outputKey = null;

    private int npix;

    @Override
    public Data process(Data input) {

        Random rand = new Random(Seed);

        Utils.mapContainsKeys(input, key);
        Utils.isKeyValid(input, key, double[].class);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        double[] data = (double[]) input.get(key);
        double[] shifted_data = new double[data.length];

        int roi = data.length / npix;

        for (int pix = 0; pix < npix; pix++) {
            int first = pix * roi;

            Double randomOffset = (rand.nextGaussian() * stdDeviation);

            //Loop over slices and shift according to random offset
            for (int slice = 0; slice < roi; slice++) {

                int shiftedSlice = slice + (int) Math.round(randomOffset);
                if (shiftedSlice < 0) {
                    shiftedSlice = shiftedSlice + roi;
                } else if (shiftedSlice >= roi) {
                    shiftedSlice = shiftedSlice - roi;
                }

                shifted_data[first + slice] = data[first + shiftedSlice];

            }

        }

        input.put(outputKey, shifted_data);
        return input;
    }
}
