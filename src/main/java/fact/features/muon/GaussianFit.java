package fact.features.muon;

import fact.Constants;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
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
    private String photonChargeKey = "photoncharge";
    @Parameter(required = false, description = "Key containing the pixel that survived cleaning", defaultValue = "shower")
    private String cleaningPixelKey = "shower";

    @Parameter(required = false, description = "Base name for the output keys", defaultValue = "gaussian_fit_")
    private String outputKey = "gaussian_fit_";

    private double[] pixel_x;
    private double[] pixel_y;


    @Override
    public Data process(Data data) {
        double[] photoncharge = (double[]) data.get(photonChargeKey);
        int[] cleaningPixel = (int[]) data.get(cleaningPixelKey);
        GaussianNegLogLikelihood negLnL = new GaussianNegLogLikelihood(photoncharge, cleaningPixel, pixel_x, pixel_y);
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
        PointValuePair result = optimizer.optimize(ob_negLnL, GoalType.MINIMIZE, start_values, maxEval);

        double[] result_point = result.getPoint();
        double r = result_point[0];
        double x = result_point[1];
        double y = result_point[2];
        double sigma = result_point[3];

        data.put(outputKey + "r", r);
        data.put(outputKey + "x", x);
        data.put(outputKey + "y", y);
        data.put(outputKey + "sigma", sigma);
        data.put(outputKey + "overlay_1", new EllipseOverlay(x, y, r + sigma, r + sigma, 0));
        data.put(outputKey + "overlay_2", new EllipseOverlay(x, y, r - sigma, r - sigma, 0));

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

    public void setPhotonChargeKey(String photonChargeKey) {
        this.photonChargeKey = photonChargeKey;
    }

    public void setCleaningPixelKey(String cleaningPixelKey) {
        this.cleaningPixelKey = cleaningPixelKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public class GaussianNegLogLikelihood implements MultivariateFunction {
        private double[] photoncharge;
        private double[] pixel_x;
        private double[] pixel_y;
        private int[] cleaning_pixel;

        /**
         * @param photoncharge double array containing the photoncharge for each pixel
         * @param cleaning_pixel int array containing all pixel chids for the pixel that survived cleaning
         * @param pixel_x double array containing the x coordinates for all pixel
         * @param pixel_y double array containing the y coordinates for all pixel
         */
        public GaussianNegLogLikelihood(double[] photoncharge, int[] cleaning_pixel, double[] pixel_x, double[] pixel_y){
            this.photoncharge = photoncharge;
            this.pixel_x = pixel_x;
            this.pixel_y = pixel_y;
            this.cleaning_pixel = cleaning_pixel;
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

            for (int i = 0; i < cleaning_pixel.length; i++) {
                int pix = cleaning_pixel[i];
                double distance = Math.sqrt(Math.pow(pixel_x[pix] - x, 2.0) + Math.pow(pixel_y[pix] - y, 2.0));
                neg_ln_L += (Math.log(sigma) + Math.pow((distance - r) / sigma, 2)) * photoncharge[pix];
            }

            return neg_ln_L;
        }
    }
}
