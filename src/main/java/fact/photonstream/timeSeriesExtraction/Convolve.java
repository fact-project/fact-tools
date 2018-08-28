package fact.photonstream.timeSeriesExtraction;

/**
 * Utility class containing a method for convolving two arrays.
 */
public class Convolve {

    /**
     * Convolves the first array with the second array. The convolution is only
     * done where both arrays are valid.
     * This corresponds to numpy.convolve(mode='valid').
     * (No cyclic edge assumptions, No zero filling)
     *
     * @param first  The first array [N].
     * @param second The second array [M].
     * @return conv
     * An array [ max(M,N) - min(M,N) + 1].
     */
    public static double[] firstWithSecond(double[] first, double[] second) {

        final int maxLength = Math.max(first.length, second.length);
        final int minLength = Math.min(first.length, second.length);
        double[] conv = new double[maxLength - minLength + 1];

        for (int i = 0; i < conv.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < second.length; j++)
                sum += first[i + j] * second[j];
            conv[i] = sum;
        }
        return conv;
    }
}
