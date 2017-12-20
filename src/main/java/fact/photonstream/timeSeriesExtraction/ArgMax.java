package fact.photonstream.timeSeriesExtraction;

/**
 * Finds the maximum on an array and stores its position (arg)
 * and its value (max).
 */
public class ArgMax {
    public int arg;
    public double max;

    /**
     * @param arr The array to find its maximum value and the maximum value's
     *            position of.
     */
    public ArgMax(double[] arr) {
        arg = 0;
        max = arr[0];
        ;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
                arg = i;
            }
        }
    }
}
