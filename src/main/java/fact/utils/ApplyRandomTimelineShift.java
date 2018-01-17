package fact.utils;

import fact.Constants;
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

    @Override
    public Data process(Data item) {

        Random rand = new Random(Seed);

        Utils.mapContainsKeys(item, key);
        Utils.isKeyValid(item, key, double[].class);

        double[] data = (double[]) item.get(key);
        double[] shifted_data = new double[data.length];

        int roi = data.length / Constants.N_PIXELS;

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
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

        item.put(outputKey, shifted_data);
        return item;
    }
}
