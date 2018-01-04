package fact.statistics.weighted;

import org.junit.Test;

import java.util.Random;

import static junit.framework.TestCase.assertEquals;

public class Weighted1dStatisticsTest {

    @Test
    public void test1d() {
        double[] x = {1, 2, 3, 4, 5};
        double[] w = {1, 1, 1, 1, 1};

        Weighted1dStatistics stats = Weighted1dStatistics.ofArrays(x, w);

        assertEquals(3.0, stats.mean, 1e-12);
        assertEquals(2.5, stats.variance, 1e-12);
        assertEquals(Math.sqrt(2.5), stats.standardDeviation, 1e-12);
        assertEquals(0.0, stats.skewness, 1e-12);
        assertEquals(1.36, stats.kurtosis, 1e-12);
    }

    public void test1dRandom() {

        Random random = new Random();
        double[] x = new double[100000];
        double[] w = new double[100000];
        for (int i = 0; i < x.length; i++) {
            x[i] = 5 * random.nextGaussian() + 2;
            w[i] = 1;
        }
        Weighted1dStatistics stats = Weighted1dStatistics.ofArrays(x, w);
        assertEquals(2.0, stats.mean, 0.05);
        assertEquals(5.0, stats.standardDeviation, 0.05);
        assertEquals(0.0, stats.skewness, 0.05);
        assertEquals(3.0, stats.kurtosis, 0.05);
    }
}
