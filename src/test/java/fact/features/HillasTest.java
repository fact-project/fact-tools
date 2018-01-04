package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.statistics.weighted.Weighted2dStatistics;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaibrugge on 27.08.14.
 */
public class HillasTest {
    HillasParameters poser = new HillasParameters();
    final double EPSILON = 1e-9;

    int[] showerPixelIds = {278, 281, 279, 282, 285, 284, 256, 259, 276, 377};
    PixelSet showerPixels = PixelSet.fromIDs(showerPixelIds);
    double[] pixelX = showerPixels.stream().mapToDouble((p) -> p.getXPositionInMM()).toArray();
    double[] pixelY = showerPixels.stream().mapToDouble((p) -> p.getYPositionInMM()).toArray();
    private double[] cog;
    private EigenDecomposition eig;
    private RealMatrix covarianceMatrix;


    double[] valuesWeighted = {1, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10};
    double[] valuesNotWeighted = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    double[] weights = {1, 1, 2, 1, 1, 1, 1, 1, 1, 3};
    private DescriptiveStatistics s = new DescriptiveStatistics();
    private double size;

    @Before
    public void setup() {
        assertEquals(weights.length, showerPixelIds.length);
        assertEquals(valuesNotWeighted.length, weights.length);

        Weighted2dStatistics stats2d = Weighted2dStatistics.ofArrays(pixelX, pixelY, weights);

        cog = stats2d.mean;
        covarianceMatrix = stats2d.covarianceMatrix;
        eig = new EigenDecomposition(covarianceMatrix);


        //get statistics
        for (double value : valuesWeighted) {
            s.addValue(value);
        }
    }

    @Test
    public void testEllipseTransformation() {
        double delta = Math.PI / 4;

        double cogX = 50;
        double cogY = 0;

        double[] P1_45 = Utils.transformToEllipseCoordinates(100, 0, cogX, cogY, delta);
        assertEquals(P1_45[0], 35.35533905932738, EPSILON);
        assertEquals(P1_45[1], -35.35533905932737, EPSILON);

        double[] P2_45 = Utils.transformToEllipseCoordinates(50, 50, cogX, cogY, delta);
        assertEquals(P2_45[0], 35.35533905932738, EPSILON);
        assertEquals(P2_45[1], 35.35533905932737, EPSILON);

        double[] P3_45 = Utils.transformToEllipseCoordinates(50, 0, cogX, cogY, delta);
        assertEquals(P3_45[0], 0, EPSILON);
        assertEquals(P3_45[1], 0, EPSILON);

        double[] P4_45 = Utils.transformToEllipseCoordinates(cogX + 20, cogY + 20, cogX, cogY, delta);
        assertEquals(P4_45[0], 28.284271247461902, EPSILON);
        assertEquals(P4_45[1], 0, EPSILON);

        double[] P5_45 = Utils.transformToEllipseCoordinates(20, -10, cogX, cogY, delta);
        assertEquals(P5_45[0], -28.284271247461902, EPSILON);
        assertEquals(P5_45[1], 14.142135623730947, EPSILON);

        double[] P6_45 = Utils.transformToEllipseCoordinates(cogX - 20, cogY - 20, cogX, cogY, delta);
        assertEquals(P6_45[0], -28.284271247461902, EPSILON);
        assertEquals(P6_45[1], 0, EPSILON);


        delta = -delta;

        double[] P1_minus45 = Utils.transformToEllipseCoordinates(100, 0, cogX, cogY, delta);
        assertEquals(P1_minus45[0], 35.35533905932738, EPSILON);
        assertEquals(P1_minus45[1], 35.35533905932738, EPSILON);

        double[] P2_minus45 = Utils.transformToEllipseCoordinates(50, 50, cogX, cogY, delta);
        assertEquals(P2_minus45[0], -35.3553390593, EPSILON);
        assertEquals(P2_minus45[1], 35.35533905932, EPSILON);

        double[] P3_minus45 = Utils.transformToEllipseCoordinates(50, 0, cogX, cogY, delta);
        assertEquals(P3_minus45[0], 0, EPSILON);
        assertEquals(P3_minus45[1], 0, EPSILON);

        double[] P4_minus45 = Utils.transformToEllipseCoordinates(cogX + 20, cogY + 20, cogX, cogY, delta);
        assertEquals(P4_minus45[0], 0, EPSILON);
        assertEquals(P4_minus45[1], 28.284271247461, EPSILON);

        double[] P5_minus45 = Utils.transformToEllipseCoordinates(20, -10, cogX, cogY, delta);
        assertEquals(P5_minus45[0], -14.14213562373, EPSILON);
        assertEquals(P5_minus45[1], -28.28427124746, EPSILON);

        double[] P6_minus45 = Utils.transformToEllipseCoordinates(cogX - 20, cogY - 20, cogX, cogY, delta);
        assertEquals(P6_minus45[0], 0, EPSILON);
        assertEquals(P6_minus45[1], -28.28427124746, EPSILON);


        delta = 20.0 / 360.0 * 2 * Math.PI;
        cogX = 0;
        cogY = 0;

        double[] P1_20 = Utils.transformToEllipseCoordinates(30, 20, cogX, cogY, delta);
        assertEquals(P1_20[0], 35.031181490090624, EPSILON);
        assertEquals(P1_20[1], 8.533248115948105, EPSILON);

        double[] P2_20 = Utils.transformToEllipseCoordinates(5, -80, cogX, cogY, delta);
        assertEquals(P2_20[0], -22.66314836212396, EPSILON);
        assertEquals(P2_20[1], -76.88551037950103, EPSILON);

        double[] P3_20 = Utils.transformToEllipseCoordinates(-35, 10, cogX, cogY, delta);
        assertEquals(P3_20[0], -29.469040294250103, EPSILON);
        assertEquals(P3_20[1], 21.367631224257497, EPSILON);

        double[] P4_20 = Utils.transformToEllipseCoordinates(-30, -30, cogX, cogY, delta);
        assertEquals(P4_20[0], -38.45138292334731, EPSILON);
        assertEquals(P4_20[1], -17.930174323807194, EPSILON);

        delta = -(80.0 / 360.0) * 2 * Math.PI;

        double[] P1_minus80 = Utils.transformToEllipseCoordinates(-10, -80, cogX, cogY, delta);
        assertEquals(P1_minus80[0], 77.04813846430, EPSILON);
        assertEquals(P1_minus80[1], -23.73993174347, EPSILON);

        double[] P2_minus80 = Utils.transformToEllipseCoordinates(30, -30, cogX, cogY, delta);

        assertEquals(P2_minus80[0], 34.75367792037415, EPSILON);
        assertEquals(P2_minus80[1], 24.33478726035833, EPSILON);

        delta = 0;
        double[] P1_0 = Utils.transformToEllipseCoordinates(-10, -80, cogX, cogY, delta);
        assertEquals(P1_0[0], -10, EPSILON);
        assertEquals(P1_0[1], -80, EPSILON);

        delta = Math.PI * 0.5;
        double[] P1_90 = Utils.transformToEllipseCoordinates(10, 20, cogX, cogY, delta);
        assertEquals(P1_90[0], 20, EPSILON);
        assertEquals(P1_90[1], -10, EPSILON);

        delta = -Math.PI * 0.5;
        double[] P1_minus90 = Utils.transformToEllipseCoordinates(10, 20, cogX, cogY, delta);
        assertEquals(P1_minus90[0], -20, EPSILON);
    }

    /**
     * If we rotate the covariance matrix by the angle delta, we should get a diagonal matrix.
     */
    @Test
    public void testDelta() {
        assertTrue("Covariance matrix should not be diagonal",
                covarianceMatrix.getEntry(0, 1) != 0 && covarianceMatrix.getEntry(1, 0) != 0);

        double delta = poser.calculateDelta(eig);

        double[][] rot = {{Math.cos(delta), Math.sin(delta)},
                {-Math.sin(delta), Math.cos(delta)}
        };

        RealMatrix rotMatrix = MatrixUtils.createRealMatrix(rot);
        RealMatrix b = (rotMatrix.multiply(covarianceMatrix)).multiply(rotMatrix.transpose());
        //check if non diagonal elements are close to zero.
        assertEquals(b.getEntry(1, 0), 0.0, EPSILON);
        assertEquals(b.getEntry(0, 1), 0.0, EPSILON);
    }
}
