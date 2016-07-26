package fact.features.singlePulse.timeLineExtraction;

import java.util.Arrays;

public class Convolve {

    public static double[] firstWithSecond(double[] timeLine, double[] pulse) {

        final int maxLength = Math.max(timeLine.length, pulse.length);
        final int minLength = Math.min(timeLine.length, pulse.length);
        double[] output = new double[maxLength - minLength + 1];

        for (int i = 0; i < output.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < pulse.length; j++)
                sum += timeLine[i+j]*pulse[j];
            output[i] = sum;
        }
        return output;
    }   
}