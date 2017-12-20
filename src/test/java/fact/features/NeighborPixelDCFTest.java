package fact.features;

import fact.extraction.NeighborPixelDCF;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by jebuss on 15.08.16.
 */
public class NeighborPixelDCFTest {

    double[] arrayA = {0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0};
    double[] arrayB = {1.0, 2.0, 1.0, 2.0, 1.0, 2.0, 1.0};
    double true_dcf = 0.3428571428571428;
    DescriptiveStatistics statsA, statsB;

    @Before
    public void setup() throws Exception {
        statsA = new DescriptiveStatistics(arrayA);
        statsB = new DescriptiveStatistics(arrayB);
    }

    @Test
    public void testUDCF() {
        double a = 2.0;
        double b = 2.0;
        double meanA = 1.0;
        double meanB = 1.0;
        double stdDevA = 2.0;
        double stdDevB = 2.0;
        double noiseA = 1.0;
        double noiseB = 1.0;

        double true_udcf = 1 / 3.0;

        NeighborPixelDCF npDCF = new NeighborPixelDCF();

        double udcfNorm = npDCF.UDCFNorm(stdDevA, stdDevB, noiseA, noiseB);

        double udcf = npDCF.UDCF(a, b, meanA, meanB, udcfNorm);

        assertTrue("UDCF should be " + true_udcf +
                " but its " + udcf, udcf == true_udcf);
    }

    @Test
    public void testDCF() {

        NeighborPixelDCF npDCF = new NeighborPixelDCF();

        double udcfNorm = npDCF.UDCFNorm(statsA.getStandardDeviation(), statsB.getStandardDeviation(), 1.0, 1.0);
        double dcf = npDCF.DCF(0, arrayA, arrayB, statsA.getMean(), statsB.getMean(), udcfNorm);

        assertTrue("DCF should be " + true_dcf +
                " but its " + dcf, dcf == true_dcf);
    }

}
