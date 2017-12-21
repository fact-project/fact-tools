package fact.statistics.weighted;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Weighted2dStatistics {
    public final int N;
    public final double weightsSum;
    public final double[] mean;
    public final double[] standardDeviation;
    public final double[] variance;
    public final double covariance;
    public final RealMatrix covarianceMatrix;

    private Weighted2dStatistics(int N, double weightsSum, double mean[], double variance[], double covariance) {
        this.N = N;
        this.weightsSum = weightsSum;
        this.mean = mean;
        this.variance = variance;
        this.covariance = covariance;
        this.covarianceMatrix = MatrixUtils.createRealMatrix(new double[][] {{variance[0], covariance}, {covariance, variance[1]}});
        this.standardDeviation = new double[] {Math.sqrt(variance[0]), Math.sqrt(variance[1])};
    }

    public static Weighted2dStatistics ofArrays (double[] x, double[] y, double[] weights) {
        if (!((x.length == y.length) & (x.length == weights.length))) {
            throw new RuntimeException("Length of arrays does not match: x: " + x.length + ", y: " + y.length + ", weights: " + weights.length);
        }

        int N = x.length;

        double[] mean = {Double.NaN, Double.NaN};
        double[] variance = {Double.NaN, Double.NaN};
        double covariance = Double.NaN;

        if (N == 0) {
            return new Weighted2dStatistics(0, 0, mean, variance, covariance);
        }

        if (N == 1) {
            mean = new double[] {x[0], y[0]};
            return new Weighted2dStatistics(1, weights[0], mean , variance, covariance);
        }

        mean = new double[] {0, 0};
        variance = new double[] {0.0, 0.0};
        double weightsSum = 0.0;
        double weightsSum2 = 0.0;
        covariance = 0.0;

        double dx;
        double dy;

        for (int i = 0; i < N; i++) {
            weightsSum += weights[i];
            weightsSum2 += Math.pow(weights[i], 2);

            dx = x[i] - mean[0];
            dy = y[i] - mean[1];

            mean[0] += (weights[i] / weightsSum) * dx;
            mean[1] += (weights[i] / weightsSum) * dy;

            variance[0] += weights[i] * dx * (x[i] - mean[0]);
            variance[1] += weights[i] * dy * (y[i] - mean[1]);

            // different treatment of x and y is intended and described here
            // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online
            covariance += weights[i] * dx * (y[i] - mean[1]);
        }

        // sample estimator
        covariance /= (weightsSum - weightsSum2 / weightsSum);
        variance[0] /= (weightsSum - weightsSum2 / weightsSum);
        variance[1] /= (weightsSum - weightsSum2 / weightsSum);

        return new Weighted2dStatistics(N, weightsSum, mean, variance, covariance);
    }
}

