package fact.utils;

/**
 * This interface is for time correction of the data array. The time distance from slice to slice varies
 * around 0.5ns and should be fixed.
 * Every implementation should take the uncalibrated data array, the times of the entries and
 * create an interpolations function for rebinning.
 *
 * @author jan.freiwald
 */
public interface TimeCorrectionKernel {
    /**
     * This function should calculate f(t), interpolating between data points.
     * The real time and value have to be sorted after time.
     *
     * @param realTime in ns
     * @param value
     */
    void fit(double[] realTime, double[] value);

    /**
     * This is f(t) and will be used for rebinning data
     *
     * @param realtime in ns
     * @return
     */
    double interpolate(double realtime);
}
