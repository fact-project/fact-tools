package fact.utils;

import fact.Constants;
import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Random;

/**
 * add random offsets to an array of arrival times. The offset is sampled from a gaussian distribution around zero
 * with a given standard deviation. The standard deviation is determined from a mean standard deviation stdDevOrigin of
 * the original (arrival time) distribution and a variance stdDevGoal, which is the mean standard deviation of the
 * desired (arrival time) distribution.
 * Created by jbuss on 28.10.14.
 */
public class ApplyRandomArrivalTimeShift implements Processor {
    static Logger log = LoggerFactory.getLogger(ApplyRandomArrivalTimeShift.class);

    @Parameter(required = true, description = "key of the arrival times array")
    public String key = null;

    @Parameter(description = "mean standard deviation of the original (arrival time) distribution")
    public double stdDevGoal = 1.33;

    @Parameter(description = "mean standard deviation of the desired (arrival time) distribution")
    public double stdDevOrigin = 0.52;

    @Parameter(description = "Seed of the random number generator")
    public long Seed = 5901;

    @Parameter(required = true, description = "key of the output array")
    public String outputKey = null;


    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, key);

        IntervalMarker[] marker = new IntervalMarker[Constants.N_PIXELS];

        double[] arrivalTime = (double[]) item.get(key);
        double[] newArrivalTime = new double[arrivalTime.length];

        Random rand = new Random(Seed);

        for (int i = 0; i < arrivalTime.length; i++) {
            Double effStdDev = Math.sqrt(stdDevGoal * stdDevGoal - stdDevOrigin * stdDevOrigin);
            Double randomArrTimeOffset = rand.nextGaussian() * effStdDev;

            newArrivalTime[i] = arrivalTime[i] + randomArrTimeOffset;
            marker[i] = new IntervalMarker(newArrivalTime[i], newArrivalTime[i] + 10);
        }

        item.put(outputKey, newArrivalTime);
        item.put(outputKey + "marker", marker);
        return item;
    }
}
