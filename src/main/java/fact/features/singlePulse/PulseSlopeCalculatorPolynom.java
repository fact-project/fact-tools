/**
 * 
 */
package fact.features.singlePulse;

import fact.Utils;
import org.apache.commons.math3.linear.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Calculates the slope of each identified pulse in the time series by fitting a third
 * order polynome to the leading edge
 * 
 *@author Jens Buss &lt;jens.buss@tu-dortmund.de&gt;
 * 
 */
public class PulseSlopeCalculatorPolynom implements Processor {
    static Logger log = LoggerFactory.getLogger(PulseSlopeCalculatorPolynom.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey = "pulsePolynom";
    //size of pulse
    @Parameter(required = true)
    private String risingEdgesKey;
    //positions of arrival times
    @Parameter(required = false, description = "number of points used for the fit", defaultValue = "11")
    private int numberOfPoints = 11;

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, key, risingEdgesKey);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        double[] data = (double[]) input.get(key);
        int roi = data.length / npix;
        int[][] risingEdges = (int[][]) input.get(risingEdgesKey);

        double[][] pulseSlopes = new double[npix][];
        double[][] arrivalTimes = new double[npix][];

        //for each pixel
        for (int pix = 0; pix < npix; pix++) {
            pulseSlopes[pix] = new double[risingEdges[pix].length];
            arrivalTimes[pix] = new double[risingEdges[pix].length];


            ArrayList<Double> slopes = new ArrayList<Double>();
            ArrayList<Double> times = new ArrayList<Double>();

            for (int pos : risingEdges[pix]) {
                int[] window = Utils.getValidWindow(pos - numberOfPoints / 2, numberOfPoints, 0, roi);
                double[] coeffn = coefficientVector(data, roi, pix, window);

                double arrivalTime = calcXPosMaxDerivation(coeffn);
                double maxSlope = calcDerivationAtPoint(arrivalTime, coeffn);

                times.add(arrivalTime);
                slopes.add(maxSlope);
            }
            pulseSlopes[pix] = Utils.arrayListToDouble(slopes);
            arrivalTimes[pix] = Utils.arrayListToDouble(times);

        }
        input.put(outputKey + "_slopes", pulseSlopes);
        input.put(outputKey + "_at", arrivalTimes);

        return input;
    }

    /**
     * Calculates the coefficient vector c fo the a third grade poliynom
     * f(x) = sum_{i=0}^3 c_i * x^i
     */
    private double[] coefficientVector(double[] data, int roi, int pix, int[] window) {
        // We do a linear least squares fit of a0 + a1*x + a2*x^2 + a3*x^3
        // Aij = fi(xj)
        // => a = (A^T * A)^(-1) * A^T * y
        int n_points = window[1] - window[0];
        double[][] arrA = new double[n_points][3 + 1];
        double[] arrY = new double[n_points];

        for (int i = 0; i < n_points; i++) {
            int x = i + window[0];
            int slice = pix * roi + x;
            for (int j = 0; j <= 3; j++) {
                arrA[i][j] = Math.pow(x, j);
                arrY[i] = data[slice];
            }
        }

        RealVector y = new ArrayRealVector(arrY);
        RealMatrix A = new Array2DRowRealMatrix(arrA);
        RealMatrix AT = A.transpose();
        RealMatrix ATA = AT.multiply(A);
        RealMatrix invATA = new LUDecomposition(ATA).getSolver().getInverse();
        RealVector a = invATA.multiply(AT).operate(y);

        return a.toArray();
    }

    /**
     * Calculates the position inflection point of the third order polynomial with coefficient vector c
     * f(x) = sum_{i=0}^3 c_i * x^i
     */
    private double calcXPosMaxDerivation(double[] c) {
        return -c[2] / c[3] / 3.0;
    }

    /**
     * Calculates the derivative of a third order polynomial  f(x) = sum_{i=0}^3 c_i * x^i
     *
     * @param x position
     * @param c coefficient vector
     * @return derivative at x for coefficient vector c
     */
    private double calcDerivationAtPoint(double x, double[] c) {
        return 3 * c[3] * x * x + 2 * c[2] * x + c[1];
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setRisingEdgesKey(String risingEdgesKey) {
        this.risingEdgesKey = risingEdgesKey;
    }

    public void setNumberOfPoints(int numberOfPoints) {
        this.numberOfPoints = numberOfPoints;
    }
}


