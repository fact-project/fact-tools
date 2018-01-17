package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.apache.commons.math3.linear.*;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class RisingEdgePolynomFit implements Processor {

    @Parameter(required = true, description = "Key to the position of the rising edges")
    public String risingEdgeKey = null;

    @Parameter(required = true, description = "Key to the data array")
    public String dataKey = null;

    @Parameter(required = true, description = "outputKey for the calculated arrival time")
    public String outputKey = null;

    @Parameter(required = true, description = "outputKey for the calculated slope at the arrival time")
    public String maxSlopesKey = null;

    @Parameter(required = false, description = "number of points used for the fit", defaultValue = "11")
    public int numberOfPoints = 11;

    @Parameter(required = false, description = "push fit results into data item", defaultValue = "false")
    public boolean showFitResult = false;


    private int fitDegree = 3;
    private double[] fitResult = null;

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, dataKey, risingEdgeKey, "NROI");

        double[] arrivalTimes = new double[Constants.N_PIXELS];
        double[] maxSlopes = new double[Constants.N_PIXELS];
        IntervalMarker[] marker = new IntervalMarker[Constants.N_PIXELS];

        double[] data = (double[]) input.get(dataKey);
        int roi = (Integer) input.get("NROI");

        double[] risingEdges = (double[]) input.get(risingEdgeKey);

        if (showFitResult) {
            fitResult = new double[roi * Constants.N_PIXELS];
        }

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            int pos = (int) risingEdges[pix];
            int[] window = Utils.getValidWindow(pos - numberOfPoints / 2, numberOfPoints, 0, roi);

            // We do a linear least squares fit of a0 + a1*x + a2*x^2 + a3*x^3
            // Aij = fi(xj)
            // => a = (A^T * A)^(-1) * A^T * y
            int n_points = window[1] - window[0];
            double[][] arrA = new double[n_points][fitDegree + 1];
            double[] arrY = new double[n_points];

            for (int i = 0; i < n_points; i++) {
                int x = i + window[0];
                int slice = pix * roi + x;
                for (int j = 0; j <= fitDegree; j++) {
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

            double[] c = a.toArray();

            arrivalTimes[pix] = calcXPosMaxDerivation(c);
            maxSlopes[pix] = calcDerivationAtPoint(arrivalTimes[pix], c);

            if (arrivalTimes[pix] < window[0]) {
                arrivalTimes[pix] = (double) window[0];
                maxSlopes[pix] = calcDerivationAtPoint(arrivalTimes[pix], c);
            } else if (arrivalTimes[pix] > window[1]) {
                arrivalTimes[pix] = (double) window[1];
                maxSlopes[pix] = calcDerivationAtPoint(arrivalTimes[pix], c);
            }

            marker[pix] = new IntervalMarker(arrivalTimes[pix], arrivalTimes[pix] + 1);

            if (showFitResult) {
                for (int i = 0; i < roi; i++) {
                    if (i < window[0] || i > window[1]) {
                        fitResult[pix * roi + i] = 0.0;
                    } else {
                        fitResult[pix * roi + i] = Polynomial(i, c);

                    }
                }
            }
        }

        input.put(outputKey, arrivalTimes);
        input.put(maxSlopesKey, maxSlopes);
        input.put(outputKey + "Marker", marker);
        if (showFitResult) {
            input.put("fitResult", fitResult);
        }


        return input;
    }

    private double Polynomial(double x, double[] c) {
        double result = 0;
        int degree = 0;
        for (double coeff : c) {
            result += coeff * Math.pow(x, degree);
            degree += 1;
        }
        return result;
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

}
