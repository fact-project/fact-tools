package fact.statistics.weighted;


public class Weighted1dStatistics {
    public final int N;
    public final double weightsSum;
    public final double mean;
    public final double standardDeviation;
    public final double variance;
    public final double m3;
    public final double m4;
    public final double skewness;
    public final double kurtosis;

    private Weighted1dStatistics(int N, double weightsSum, double mean, double variance, double m3, double m4) {
        this.N = N;
        this.weightsSum = weightsSum;
        this.mean = mean;
        this.variance = variance;
        this.standardDeviation = Math.sqrt(variance);
        this.m3 = m3;
        this.m4 = m4;
        this.skewness = m3 / Math.pow(standardDeviation, 3.0);
        this.kurtosis = m4 / Math.pow(standardDeviation, 4.0);
    }

    public static Weighted1dStatistics ofArrays (double[] x, double[] weights) {
        if (x.length != weights.length) {
            throw new RuntimeException("Length of arrays does not match: x: " + x.length + ", weights: " + weights.length);
        }

        int N = x.length;

        if (N == 0) {
            return new Weighted1dStatistics(0, 0.0, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }
        if (N == 1) {
            return new Weighted1dStatistics(1, weights[0], x[0], Double.NaN, Double.NaN, Double.NaN);
        }

        double mean = 0.0;
        double weightsSum = 0.0;
        double weightsSum2 = 0.0;
        double dx;
        double m2 = 0.0;
        double m3 = 0.0;
        double m4 = 0.0;

        for (int i = 0; i < N; i++) {
            weightsSum += weights[i];
            weightsSum2 += Math.pow(weights[i], 2);

            dx = x[i] - mean;
            mean += (weights[i] / weightsSum) * dx;
            m2 += weights[i] * dx * (x[i] - mean);
        }

        for (int i = 0; i < N; i++) {
            dx = x[i] - mean;
            m3 += weights[i] * Math.pow(dx, 3);
            m4 += weights[i] * Math.pow(dx, 4);
        }

        // sample estimator
        m2 /= (weightsSum - weightsSum2 / weightsSum);
        m3 /= (weightsSum - weightsSum2 / weightsSum);
        m4 /= (weightsSum - weightsSum2 / weightsSum);

        return new Weighted1dStatistics(N, weightsSum, mean, m2, m3, m4);
    }
}

