package fact.utils;

import java.util.Arrays;

/**
 * This class does an linear interpolation of input points.
 *
 * @author jan, kai, max
 */
public class LinearTimeCorrectionKernel implements TimeCorrectionKernel {

    private int numPoints = 0;
    private double[] times = null;
    private double[] values = null;

    @Override
    public void fit(double[] realTime, double[] value) {
        numPoints = realTime.length;
        times = realTime;
        values = value;
    }

    /**
     * This calculates a linear interpolation of $[t,v]_i$ for all $i$.
     */
    @Override
    public double interpolate(double t) {

        int pos = Arrays.binarySearch(times, t);

        if (pos >= 0) {
            return values[pos];
        }

        // see semantic of Arrays.binaryseach return code.
        pos = -(pos + 1);

        // in case insertion point is the beginning of the array return the left border
        if (pos == 0) {
            return values[0];
        }

        if (pos >= values.length) {
            return values[values.length - 1];
        }


        double t0 = times[pos - 1];
        double t1 = times[pos];

        // calculate the slope
        double s = (t - t0) / (t1 - t0);

        double v0 = values[pos - 1];
        double v1 = values[pos];

        return (v1 - v0) * s + v0;

    }
}
