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
import fact.Utils;

/*import fact.features.DistributionFromShower;*/


/**
 * Created by thomno on 13.06.2016
 */
public class GaussianFit2D implements StatefulProcessor {
    /*
     * This Process calculate with a 2D Gaus new cov_ and new x and y.
     * With the new cov_ Array it calculate the new Delta, Length and width
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
    private String deltabeforKey = null;
	@Parameter(required=true)
	private int cutdouble;

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
        double deltabefor = (Double) data.get(deltabeforKey);


        // Calculate size vor later
        double size = 0;
        for (double v : showerWeights) {
            size += v;
        }

        String cutstring = String.valueOf(cutdouble);


        // Calculate cov_
        double[][] covarianceMatrix = calculateCovarianceMatrix(pixelSet.toIntArray(), photoncharge, cogx, cogy);
        data.put(outputKey + "cov_11_vorher", covarianceMatrix[0][0]);
        data.put(outputKey + "cov_12_vorher", covarianceMatrix[0][1]);
        data.put(outputKey + "cov_22_vorher", covarianceMatrix[1][1]);
        data.put(outputKey + "size", size);

        //Save all pixel vor python
        int maxPixelID = -1;
        double maxPhotoncharge = -1000;
        for (CameraPixel pix: pixelSet.set) {
            if (maxPhotoncharge < photoncharge[pix.id]) {
                maxPhotoncharge = photoncharge[pix.id];
                maxPixelID = pix.id;
            }
        }

        String outkey = "Gaussian_2D_0." + cutstring;
        if (cutdouble == 99){
            outkey = "Gaussian_2D_all";
        }
        if (cutdouble == 98){
            outkey = "Gaussian_2D_mc";
        }
        // Evaluation
        GaussianNegLogLikelihood negLnL = new GaussianNegLogLikelihood(photoncharge, pixelSet, maxPhotoncharge);
        ObjectiveFunction ob_negLnL = new ObjectiveFunction(negLnL);

        MaxEval maxEval = new MaxEval(10000000);
        InitialGuess start_values = new InitialGuess(new double[] {getx(maxPixelID), gety(maxPixelID), covarianceMatrix[0][0], covarianceMatrix[0][1], covarianceMatrix[1][1]});
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
        Double cov_11 = result_point[2];
        Double cov_12 = result_point[3];
        Double cov_22 = result_point[4];

        //Save Data
        data.put(outkey + "x", x);
        data.put(outkey + "y", y);
        data.put(outkey + "cov_11", cov_11);
        data.put(outkey + "cov_12", cov_12);
        data.put(outkey + "cov_22", cov_22);

        //Continue if not NaN
        int ditit = 0;
        if (!x.isNaN()) {
            ditit = 1;
            //create covarianceMatrix
            double[][] matrixData = { { cov_11, cov_12 }, { cov_12, cov_22 } };

            // create RealMatrix vor later
            RealMatrix cov_Matrix = MatrixUtils.createRealMatrix(matrixData);

            //calculate EigenDecomposition
            EigenDecomposition eig = new EigenDecomposition(cov_Matrix);
            double varianceLong = eig.getRealEigenvalue(0);
            double varianceTrans = eig.getRealEigenvalue(1);
            //Division with size is not right and not wrong
            //double varianceLong = eig.getRealEigenvalue(0);
            //double varianceTrans = eig.getRealEigenvalue(1);

            //calculate length and width
            double length = Math.sqrt(varianceLong);
            double width = Math.sqrt(varianceTrans);

            // calculate delta
            double delta = calculateDelta(eig);

            //save data 2

            data.put(outkey + "width", width);
            data.put(outkey + "length", length);
            data.put(outkey + "delta", delta);
            data.put(outkey + "deltadiff", Math.abs(delta - deltabefor));
            //System.out.println(delta - deltabefor);
            //System.out.println(outkey + "deltadiff");
            //System.out.println(delta);
            //System.out.println(deltabefor);
            data.put(outkey + "overlay_1", new EllipseOverlay(x, y, width, length, delta));
            data.put(outkey + "overlay_2", new EllipseOverlay(x, y, 2*width, 2*length, delta));

        }
        data.put(outkey + "ditit", ditit);
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

    public String getDeltabeforKey() {
        return deltabeforKey;
    }

    public void setDeltabeforKey(String deltabeforKey) {
        this.deltabeforKey = deltabeforKey;
    }

	public int getCutdouble() {
		return cutdouble;
	}

	public void setCutdouble(int cutdouble) {
		this.cutdouble = cutdouble;
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
        double sum_of_weights = 0;
        for (int pix : showerPixel) {
            double weight = photoncharge[pix];
            double posx = pixelMap.getPixelFromId(pix).getXPositionInMM();
            double posy = pixelMap.getPixelFromId(pix).getYPositionInMM();

            sum_of_weights += weight;

            variance_xx += weight * (posx - cogx) * (posx - cogx);
            variance_yy += weight * (posy - cogy) * (posy - cogy);
            covariance_xy += weight * (posx - cogx) * (posy - cogy);

            i++;
        }

        double[][] matrixData = { { variance_xx / sum_of_weights, covariance_xy / sum_of_weights}, { covariance_xy / sum_of_weights, variance_yy / sum_of_weights} };
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
        private double[] photoncharge;
        private PixelSet pixelSet;
        private double maxPhotoncharge;

        /**
         *
         * get from all Point in Shower x, y, and the photoncharge
         */
        public GaussianNegLogLikelihood(double[] photoncharge, PixelSet pixelSet, double maxPhotoncharge){
            this.photoncharge = photoncharge;
            this.pixelSet = pixelSet;
            this.maxPhotoncharge = maxPhotoncharge;
        }

        /**
         *
         * @param point a double array with length 5 containing x, y, cov_11, cov_12, cov_22  in this order
         * @return the negative log likelihood at this point for the given data
         */
        public double value(double[] point) {
            double mu_x = point[0];
            double mu_y = point[1];
            double cov_11 = point[2];
            double cov_12 = point[3];
            double cov_22 = point[4];
            double neg_ln_L = 0;
            for(CameraPixel pixel: pixelSet.set){
                double x = getx(pixel.id)  - mu_x;
                double y= gety(pixel.id) - mu_y;
                double term1 = cov_11 * cov_22 - Math.pow(cov_12, 2.0);
                double term2 = 0.5 / term1 * (Math.pow(x, 2.0) * cov_22 + Math.pow(y, 2.0) * cov_11 - 2 * cov_12 * x * y);
                /*
                if (photon_log[i] > photon_max){
                    neg_ln_L += (term1 + term2) * photon_log[i] ;
                }
                */
                neg_ln_L += (0.5 * Math.log(term1) + term2) * photoncharge[pixel.id];
            }
            return neg_ln_L;
        }
    }
}
