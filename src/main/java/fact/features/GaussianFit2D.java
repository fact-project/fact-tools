package fact.features;

import fact.Constants;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import java.io.*;
import fact.Utils;

/*import fact.features.DistributionFromShower;*/

/**
 * Created by thomno on 13.06.2016
 */
public class GaussianFit2D implements StatefulProcessor {
    /*
     * This Process calculate with a 2D Gaus new sigma and new x and y.
     * With the new sigma Array it calculate the new Delta, Length and width
     */
    @Parameter(required = false, description = "Key containing the photoncharges", defaultValue = "photoncharge")
    private String photonchargeKey = "photoncharge";
    @Parameter(required = false, description = "The pixelSet on which the fit is performed", defaultValue = "shower")
    private String pixelSetKey = "shower";
    @Parameter(required=false, description="key to the xvalue of the cog of the shower")
    private String cogxKey = null;
    @Parameter(required=false, description="key to the yvalue of the cog of the shower")
    private String cogyKey = null;
    @Parameter(required=false, description="key to the yvalue of the cog of the shower")
    private String deltavorherKey = null;

    @Parameter(required = false, description = "Base name for the output keys", defaultValue = "Gaussian_2D_")
    private String outputKey = "Gaussian_2D_";

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();


    @Override
    public Data process(Data data) {
        //double[] photoncharge = (double[]) data.get(photonchargeKey);
        double[] photoncharge = Utils.toDoubleArray(data.get(photonchargeKey));

        PixelSet pixelSet = (PixelSet) data.get(pixelSetKey);
        double[] showerWeights = createShowerWeights(pixelSet.toIntArray(),
            photoncharge);

        //Get Center
        double cogx = (Double) data.get(cogxKey);
        double cogy = (Double) data.get(cogyKey);
        double deltavorher = (Double) data.get(deltavorherKey);


        // Calculate size vor later
        double size = 0;
        for (double v : showerWeights) {
            size += v;
        }

        // Calculate sigma
        double[][] covarianceMatrix = calculateCovarianceMatrix(pixelSet.toIntArray(), photoncharge, cogx, cogy);
        data.put(outputKey + "sigma11_vorher", covarianceMatrix[0][0]);
        data.put(outputKey + "sigma12_vorher", covarianceMatrix[0][1]);
        data.put(outputKey + "sigma22_vorher", covarianceMatrix[1][1]);
        data.put(outputKey + "size", size);

        /*
        //Test ob funktionen stimmen sie stimmen also stimmt was in der Berechnung unten nicht
        RealMatrix sigma_Matrix_test = MatrixUtils.createRealMatrix(covarianceMatrix);

        //calculate EigenDecomposition
        EigenDecomposition eig_Test = new EigenDecomposition(sigma_Matrix_test);
        double varianceLong_Test = eig_Test.getRealEigenvalue(0) / size;
        double varianceTrans_Test = eig_Test.getRealEigenvalue(1) / size;

        //calculate length and width
        double length_Test = Math.sqrt(varianceLong_Test);
        double width_Test = Math.sqrt(varianceTrans_Test);

        // calculate delta
        double delta_Test = calculateDelta(eig_Test);
        //data.put(outputKey + "overlay_1_Test", new EllipseOverlay(cogx, cogy, width_Test, length_Test, delta_Test));
        //data.put(outputKey + "overlay_2_Test", new EllipseOverlay(cogx, cogy, 2*width_Test, 2*length_Test, delta_Test));
        */

        //Save all pixel vor python
        int npix = Constants.NUMBEROFPIXEL;
        double[] array_x = new double[npix];
        double[] array_y = new double[npix];
        double[] photon = new double[npix];

        for (int pix = 0; pix < npix; pix++) {
            array_x[pix] = 0;
            array_y[pix] = 0;
            photon[pix] = 0;
        }
        int zahl = 0;
        double photon_x_max = 0;
        double photon_y_max = 0;
        double photon_max = -1000;
        System.out.println();
        for (CameraPixel pix: pixelSet.set){
            array_x[zahl] = getx(pix.id);
            array_y[zahl] = gety(pix.id);
            photon[zahl] = photoncharge[pix.id];
            //System.out.println(photoncharge[pix.id]);
            if (photon_max < photoncharge[pix.id]){
                photon_max = photoncharge[pix.id];
                photon_x_max = getx(pix.id);
                photon_y_max = gety(pix.id);
            }
            zahl = zahl + 1;
        }

        data.put(outputKey + "wax", array_x);
        data.put(outputKey + "way", array_y);
        data.put(outputKey + "wapho", photon);


        // Evaluation
        GaussianNegLogLikelihood negLnL = new GaussianNegLogLikelihood(array_x, array_y, photon, photon_max);
        ObjectiveFunction ob_negLnL = new ObjectiveFunction(negLnL);

        MaxEval maxEval = new MaxEval(10000000);
        InitialGuess start_values = new InitialGuess(new double[] {photon_x_max, photon_y_max, covarianceMatrix[0][0], covarianceMatrix[0][1], covarianceMatrix[1][1]});
        PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-2);
        PointValuePair result;
        try {
            result = optimizer.optimize(ob_negLnL, GoalType.MINIMIZE, start_values, maxEval);
        } catch (TooManyEvaluationsException e){
            result = new PointValuePair(new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN}, Double.NaN);
        }
        double[] result_point = result.getPoint();

        // Get Data
        Double x = result_point[0];
        Double y = result_point[1];
        Double sigma11 = result_point[2];
        Double sigma12 = result_point[3];
        Double sigma22 = result_point[4];

        //Save Data
        data.put(outputKey + "x", x);
        data.put(outputKey + "y", y);
        data.put(outputKey + "sigma11", sigma11);
        data.put(outputKey + "sigma12", sigma12);
        data.put(outputKey + "sigma22", sigma22);

        //Continue if not NaN
        if (!x.isNaN()) {
            //create covarianceMatrix
            double[][] matrixData = { { sigma11, sigma12 }, { sigma12, sigma22 } };

            // create RealMatrix vor later
            RealMatrix sigma_Matrix = MatrixUtils.createRealMatrix(matrixData);

            //calculate EigenDecomposition
            EigenDecomposition eig = new EigenDecomposition(sigma_Matrix);
            double varianceLong = eig.getRealEigenvalue(0) / size;
            double varianceTrans = eig.getRealEigenvalue(1) / size;
            //Division with size is not right and not wrong
            //double varianceLong = eig.getRealEigenvalue(0);
            //double varianceTrans = eig.getRealEigenvalue(1);

            //calculate length and width
            double length = Math.sqrt(varianceLong);
            double width = Math.sqrt(varianceTrans);

            // calculate delta
            double delta = calculateDelta(eig);

            //save data 2

            data.put(outputKey + "width", width);
            data.put(outputKey + "length", length);
            data.put(outputKey + "delta", delta);
            data.put(outputKey + "deltaunter", Math.abs(delta - deltavorher));
            System.out.println(delta - deltavorher);
            //System.out.println(outputKey + "deltaunter");
            //System.out.println(delta);
            //System.out.println(deltavorher);
            data.put(outputKey + "overlay_1", new EllipseOverlay(x, y, width, length, delta));
            data.put(outputKey + "overlay_2", new EllipseOverlay(x, y, 2*width, 2*length, delta));

        }
        return data;
    }

    public void init(ProcessContext context) {
        int npix = Constants.NUMBEROFPIXEL;
        double[] pixel_x = new double[npix];
        double[] pixel_y = new double[npix];

        FactPixelMapping mapping = FactPixelMapping.getInstance();

        for (int pix = 0; pix < npix; pix++) {
            pixel_x[pix] = mapping.getPixelFromId(pix).getXPositionInMM();
            pixel_y[pix] = mapping.getPixelFromId(pix).getYPositionInMM();
        }
    }

    public void finish(){}

    public void resetState(){}

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

    public void setPhotonchargeKey(String photonchargeKey) {
        this.photonchargeKey = photonchargeKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getCogxKey() {
        return cogxKey;
    }

    public void setCogxKey(String cogxKey) {
        this.cogxKey = cogxKey;
    }

    public String getCogyKey() {
        return cogyKey;
    }

    public void setCogyKey(String cogyKey) {
        this.cogyKey = cogyKey;
    }

    public String getDeltavorherKey() {
        return deltavorherKey;
    }

    public void setDeltavorherKey(String deltavorherKey) {
        this.deltavorherKey = deltavorherKey;
    }

    public double[] createShowerWeights(int[] shower, double[] pixelWeights) {
        double[] weights = new double[shower.length];
        for (int i = 0; i < shower.length; i++) {
            weights[i] = pixelWeights[shower[i]];
        }
        return weights;
    }

    public double[][] calculateCovarianceMatrix(int[] showerPixel,
        double[] photoncharge, double cogx, double cogy) {
        double variance_xx = 0;
        double variance_yy = 0;
        double covariance_xy = 0;
        int i = 0;
        for (int pix : showerPixel) {
            double weight = photoncharge[pix];
            double posx = pixelMap.getPixelFromId(pix).getXPositionInMM();
            double posy = pixelMap.getPixelFromId(pix).getYPositionInMM();

            variance_xx += weight * (posx - cogx) * (posx - cogx);
            variance_yy += weight * (posy - cogy) * (posy - cogy);
            covariance_xy += weight * (posx - cogx) * (posy - cogy);

            i++;
        }

        double[][] matrixData = { { variance_xx, covariance_xy }, { covariance_xy, variance_yy } };
        return matrixData;
    }

    public double calculateDelta(EigenDecomposition eig) {
        // calculate the angle between the eigenvector and the camera axis.
        // So basicly the angle between the major-axis of the ellipse and the
        // camrera axis.
        // this will be written in radians.
        double longitudinalComponent = eig.getEigenvector(0).getEntry(0);
        double transversalComponent = eig.getEigenvector(0).getEntry(1);
        return Math.atan(transversalComponent / longitudinalComponent);
    }


    double getx (int id){
        return pixelMap.getPixelFromId(id).getXPositionInMM();
    }

    double gety (int id){
        return pixelMap.getPixelFromId(id).getYPositionInMM();
    }

    public class GaussianNegLogLikelihood implements MultivariateFunction {
        private double[] array_x_log;
        private double[] array_y_log;
        private double[] photon_log;
        private double photon_max;

        /**
         *
         * get from all Point in Shower x, y, and the photoncharge
         */
        public GaussianNegLogLikelihood(double[] array_x,double[] array_y,double[] photon, double photon_max){
            this.array_x_log = array_x;
            this.array_y_log = array_y;
            this.photon_log = photon;
            this.photon_max = photon_max;
        }

        /**
         *
         * @param point a double array with length 5 containing x, y, sigma11, sigma12, sigma22  in this order
         * @return the negative log likelihood at this point for the given data
         */
        public double value(double[] point) {
            double x_0 = point[0];
            double y_0 = point[1];
            double sigma_11 = point[2];
            double sigma_12 = point[3];
            double sigma_22 = point[4];
            double neg_ln_L = 0;

            //Test
            /*
            for (CameraPixel pix: pixelSet.set) {
                double x_1 = getx(pix.id) - x_0;
                double y_1 = gety(pix.id) - y_0;
                double term1 = 1 / 2 * Math.log(Math.pow(sigma_11, 2.0) * Math.pow(sigma_22, 2.0) - Math.pow(sigma_12, 2.0));
                double term2 = 1 / 2 * 1 / (Math.pow(sigma_11, 2.0) * Math.pow(sigma_22, 2.0) - Math.pow(sigma_12, 2.0)) * (Math.pow(x_1, 2.0) * Math.pow(sigma_22, 2.0) + Math.pow(y_1, 2.0) * Math.pow(sigma_11, 2.0) - 2 * sigma_12 * x_1 * y_1);
                neg_ln_L += (term1 + term2) * photoncharge[pix.id];
            }
            */
            // Normal
            int npix = Constants.NUMBEROFPIXEL;
            for(int i=0; i<npix; i++){
                if (array_x_log[i] == 0 && array_x_log[i + 1] == 0){
                    break;
                }
                double x_1 = array_x_log[i] - x_0;
                double y_1 = array_y_log[i] - y_0;
                double term1 = 1 / 2 * Math.log(sigma_11 * sigma_22 - Math.pow(sigma_12, 2.0));
                double term2 = 1 / 2 * 1 / (sigma_11 * sigma_22 - Math.pow(sigma_12, 2.0)) * (Math.pow(x_1, 2.0) * sigma_22 + Math.pow(y_1, 2.0) * sigma_11 - 2 * sigma_12 * x_1 * y_1);
                /*
                if (photon_log[i] > photon_max){
                    neg_ln_L += (term1 + term2) * photon_log[i] ;
                }
                */
                neg_ln_L += (term1 + term2) * photon_log[i] ;
            }
            return neg_ln_L;
        }
    }
}
