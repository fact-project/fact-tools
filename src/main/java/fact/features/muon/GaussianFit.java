package fact.features.muon;

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
import org.apache.commons.math3.util.DoubleArray;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/**
 * Created by maxnoe on 13.08.15.
 */
public class GaussianFit implements StatefulProcessor {
    @Parameter(required = false, description = "Start value for the radius for the likelihood fit", defaultValue = "120")
    private String startRKey = "";
    @Parameter(required = false, description = "Start value for X for the likelihood fit", defaultValue = "0")
    private String startXKey = "";
    @Parameter(required = false, description = "Start value for Y for the likelihood fit", defaultValue = "0")
    private String startYKey = "";
    @Parameter(required = false, description = "Start value for Y for the likelihood fit", defaultValue = "5")
    private String startSigmaKey = "";
    @Parameter(required = false, description = "Key containing the photoncharges", defaultValue = "photoncharge")
    private String photonchargeKey = "photoncharge";
    @Parameter(required = false, description = "The pixelSet on which the fit is performed", defaultValue = "shower")
    private String pixelSetKey = "shower";

    @Parameter(required = false, description = "Base name for the output keys", defaultValue = "gaussian_fit_")
    private String outputKey = "gaussian_fit_";

    private double[] pixel_x;
    private double[] pixel_y;


    @Override
    public Data process(Data data) {
        double[] photoncharge = (double[]) data.get(photonchargeKey);
        PixelSet pixelSet = (PixelSet) data.get(pixelSetKey);
        GaussianNegLogLikelihood negLnL = new GaussianNegLogLikelihood(photoncharge, pixelSet, pixel_x, pixel_y);
        ObjectiveFunction ob_negLnL = new ObjectiveFunction(negLnL);

        double startR = 120,
                startX = 0,
                startY = 0,
                startSigma = 5;

        if (!startRKey.isEmpty()){
            startR = (double) data.get(startRKey);
        }
        if (!startXKey.isEmpty()){
            startX = (double) data.get(startXKey);
        }
        if (!startYKey.isEmpty()){
            startY = (double) data.get(startYKey);
        }
        if (!startSigmaKey.isEmpty()){
            startSigma = (double) data.get(startSigmaKey);
        }

        MaxEval maxEval = new MaxEval(10000);
        InitialGuess start_values = new InitialGuess(new double[] {startR, startX, startY, startSigma});
        PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-2);

        PointValuePair result;
        try {
            result = optimizer.optimize(ob_negLnL, GoalType.MINIMIZE, start_values, maxEval);
        } catch (TooManyEvaluationsException e){
            result = new PointValuePair(new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN}, Double.NaN);
        }
        double[] result_point = result.getPoint();
        Double r = Math.abs(result_point[0]);
        Double x = result_point[1];
        Double y = result_point[2];
        Double sigma = Math.abs(result_point[3]);

        data.put(outputKey + "r", r);
        data.put(outputKey + "x", x);
        data.put(outputKey + "y", y);
        data.put(outputKey + "sigma", sigma);

        if (!r.isNaN()) {
            data.put(outputKey + "overlay_1", new EllipseOverlay(x, y, r + sigma, r + sigma, 0));
            data.put(outputKey + "overlay_2", new EllipseOverlay(x, y, r - sigma, r - sigma, 0));
        }
        return data;
    }

    public void init(ProcessContext context) {
        int npix = Constants.NUMBEROFPIXEL;
        pixel_x = new double[npix];
        pixel_y = new double[npix];

        FactPixelMapping mapping = FactPixelMapping.getInstance();

        for (int pix = 0; pix < npix; pix++) {
            pixel_x[pix] = mapping.getPixelFromId(pix).getXPositionInMM();
            pixel_y[pix] = mapping.getPixelFromId(pix).getYPositionInMM();
        }
    }

    public void finish(){}

    public void resetState(){}

    public void setStartRKey(String startRKey) {
        this.startRKey = startRKey;
    }

    public void setStartXKey(String startXKey) {
        this.startXKey = startXKey;
    }

    public void setStartYKey(String startYKey) {
        this.startYKey = startYKey;
    }

    public void setStartSigmaKey(String startSigmaKey) {
        this.startSigmaKey = startSigmaKey;
    }

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

    public void setPhotonchargeKey(String photonchargeKey) {
        this.photonchargeKey = photonchargeKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public class GaussianNegLogLikelihood implements MultivariateFunction {
        private double[] photoncharge;
        private double[] pixel_x;
        private double[] pixel_y;
        private PixelSet pixelSet;

        /**
         * @param photoncharge double array containing the photoncharge for each pixel
         * @param pixelSet int array containing all pixel chids for the pixel that survived cleaning
         * @param pixel_x double array containing the x coordinates for all pixel
         * @param pixel_y double array containing the y coordinates for all pixel
         */
        public GaussianNegLogLikelihood(double[] photoncharge, PixelSet pixelSet, double[] pixel_x, double[] pixel_y){
            this.photoncharge = photoncharge;
            this.pixel_x = pixel_x;
            this.pixel_y = pixel_y;
            this.pixelSet = pixelSet;
        }

        /**
         *
         * @param point a double array with length 4 containing r, x, y, sigma in this order
         * @return the negative log likelihood at this point for the given data
         */
        public double value(double[] point) {
            double r = point[0];
            double x = point[1];
            double y = point[2];
            double sigma = point[3];
            double neg_ln_L = 0;

            for (CameraPixel pix: pixelSet.set) {
                double distance = Math.sqrt(Math.pow(pixel_x[pix.id] - x, 2.0) + Math.pow(pixel_y[pix.id] - y, 2.0));
                neg_ln_L += (Math.log(sigma) + 0.5 * Math.pow((distance - r) / sigma, 2)) * photoncharge[pix.id];
            }

            return neg_ln_L;
        }
    }
}