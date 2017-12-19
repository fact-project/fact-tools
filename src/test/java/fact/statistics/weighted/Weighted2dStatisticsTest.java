package fact.statistics.weighted;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class Weighted2dStatisticsTest {

    @Test
    public void testWeightedStatistics () {
        // values tested against numpy.cov(x, y, aweights=weights, ddof=1)
        double[] x = {0, 1, 1, 2};
        double[] y = {5, 6, 7, 8};
        double[] weights = {1.0, 2.0, 2.0, 1.0};

        Weighted2dStatistics statistics = Weighted2dStatistics.ofArrays(x, y, weights);

        assertEquals(4, statistics.N);
        assertEquals(6.0, statistics.weightsSum, 1e-12);
        assertEquals(1.0, statistics.mean[0], 1e-12);
        assertEquals(6.5, statistics.mean[1], 1e-12);
        assertEquals(0.46153846153846156, statistics.variance[0], 1e-12);
        assertEquals(1.2692307692307694, statistics.variance[1], 1e-12);
        assertEquals(0.69230769230769229, statistics.covariance, 1e-12);
    }

    @Test
    public void testNan() {
        double[] x1 = {};
        double[] y1 = {};
        double[] weights1 = {};

        Weighted2dStatistics statistics1 = Weighted2dStatistics.ofArrays(x1, y1, weights1);
        assertEquals(0, statistics1.N);
        assertEquals(0.0, statistics1.weightsSum);
        assertTrue(Double.isNaN(statistics1.mean[0]));
        assertTrue(Double.isNaN(statistics1.mean[1]));
        assertTrue(Double.isNaN(statistics1.standardDeviation[0]));
        assertTrue(Double.isNaN(statistics1.standardDeviation[1]));
        assertTrue(Double.isNaN(statistics1.variance[0]));
        assertTrue(Double.isNaN(statistics1.variance[1]));
        assertTrue(Double.isNaN(statistics1.covariance));

        double[] x2 = {1, };
        double[] y2 = {1, };
        double[] weights2 = {2.0, };

        Weighted2dStatistics statistics2 = Weighted2dStatistics.ofArrays(x2, y2, weights2);
        assertEquals(1, statistics2.N);
        assertEquals(2.0, statistics2.weightsSum);
        assertEquals(x2[0], statistics2.mean[0], 1e-16);
        assertEquals(y2[0], statistics2.mean[1], 1e-16);
        assertTrue(Double.isNaN(statistics2.standardDeviation[0]));
        assertTrue(Double.isNaN(statistics2.standardDeviation[1]));
        assertTrue(Double.isNaN(statistics2.variance[0]));
        assertTrue(Double.isNaN(statistics2.variance[1]));
        assertTrue(Double.isNaN(statistics2.covariance));

    }
}
