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
public class SmearOutArrivalTimes implements Processor{
    static Logger log = LoggerFactory.getLogger(SmearOutArrivalTimes.class);

    @Parameter(required = true, description = "key of the arrival times array")
    private String key = null;

    @Parameter(description = "mean standard deviation of the original (arrival time) distribution")
    private double stdDevGoal   = 1.33;

    @Parameter(description = "mean standard deviation of the desired (arrival time) distribution")
    private double stdDevOrigin = 0.52;

    @Parameter(required = true, description = "key of the output array")
    private String outputKey = null;

    Random rand = new Random();
    private double[] arrivalTime = null;
    private double[] newArrivalTime = null;


    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, key);

        IntervalMarker[] marker = new IntervalMarker[Constants.NUMBEROFPIXEL];

        arrivalTime     = (double[]) input.get(key);
        newArrivalTime  = new double[arrivalTime.length];

        for ( int i = 0; i < arrivalTime.length; i++){
            Double effStdDev = Math.sqrt(stdDevGoal * stdDevGoal - stdDevOrigin * stdDevOrigin);
            Double randomArrTimeOffset = rand.nextGaussian()* effStdDev;

            newArrivalTime[i] = arrivalTime[i] + randomArrTimeOffset;
            marker[i] = new IntervalMarker(newArrivalTime[i], newArrivalTime[i]+10);
        }

        input.put(outputKey, newArrivalTime);
        input.put(outputKey+"marker", marker);
        return input;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public double getStdDevOrigin() {
        return stdDevOrigin;
    }

    public void setStdDevOrigin(double stdDevOrigin) {
        this.stdDevOrigin = stdDevOrigin;
    }

    public double getStdDevGoal() {
        return stdDevGoal;
    }

    public void setStdDevGoal(double stdDevGoal) {
        this.stdDevGoal = stdDevGoal;
    }
}
