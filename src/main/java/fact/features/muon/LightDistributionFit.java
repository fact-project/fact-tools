package fact.features.muon;

import fact.Constants;
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
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by maxnoe on 17.08.15.
 */
public class LightDistributionFit implements Processor {

    @Parameter(required = false, description = "key containing the photoncharges", defaultValue = "photoncharge")
    public  String photonchargeKey = "photoncharge";

    @Parameter(required = false, description = "key containing the pixel that survided cleaning", defaultValue = "shower")
    public  String cleaningPixelKey = "shower";

    @Parameter(required = false, description = "key containing the initial guess for the radius, if not given 100mm is used", defaultValue = "null")
    public  String initialRKey = null;

    @Parameter(required = false, description = "key containing the initial guess for center x, if not given 0 is used", defaultValue = "null")
    public  String initialXKey = null;

    @Parameter(required = false, description = "key containing the initial guess for center y, if not given 0 is used", defaultValue = "null")
    public  String initialYKey = null;

    @Parameter(required = false, description = "outputkey basis, r,x,y,rho,phi,eps are appended to it", defaultValue = "muonfit_")
    public  String outputKey = "muonfit_";

    private double initialR = 100;
    private double initialX = 0;
    private double initialY = 0;
    private double initialSigma = 5;
    private double initialRho = 0;
    private double initialPhi = 0;
    private double initialEps = 0.2;

    @Override
    public Data process(Data data) {

        if (initialRKey != null) {
            initialR = (double) data.get(initialRKey);
        }
        if (initialXKey != null) {
            initialX = (double) data.get(initialXKey);
        }
        if (initialYKey != null) {
            initialX = (double) data.get(initialYKey);
        }

        double[] photoncharge = (double[]) data.get(photonchargeKey);
        int[] cleaningPixel = (int[]) data.get(cleaningPixelKey);

        LightDistributionNegLogLikelihood negLnL = new LightDistributionNegLogLikelihood(photoncharge, cleaningPixel);
        ObjectiveFunction ob_negLnL = new ObjectiveFunction(negLnL);

        MaxEval maxEval = new MaxEval(50000);
        double[] initials = new double[]{
                initialR,
                initialX,
                initialY,
                initialSigma,
                initialRho,
                initialPhi,
                initialEps,
        };

        InitialGuess start_values = new InitialGuess(initials);
        PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-2);

        double[] result_point = new double[initials.length];
        try {
            PointValuePair result = optimizer.optimize(ob_negLnL, GoalType.MINIMIZE, start_values, maxEval);
            result_point = result.getPoint();
        } catch (TooManyEvaluationsException e) {
            for (int i = 0; i < result_point.length; i++) {
                result_point[i] = Double.NaN;
            }
        }


        double r = result_point[0];
        double x = result_point[1];
        double y = result_point[2];
        double sigma = result_point[3];
        double rho = result_point[4];
        double phi = result_point[5];
        double eps = result_point[6];
        double[] test = new double[Constants.N_PIXELS];
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            test[pix] = negLnL.photon_expectance_pixel(pix, r, x, y, sigma, rho, phi, eps);
        }
        data.put("photon_expectance_fit", test);

        data.put(outputKey + "r", r);
        data.put(outputKey + "x", x);
        data.put(outputKey + "y", y);
        data.put(outputKey + "sigma", sigma);
        data.put(outputKey + "rho", rho);
        data.put(outputKey + "phi", phi);
        data.put(outputKey + "eps", eps);
        if (result_point[0] != Double.NaN) {
            data.put(outputKey + "circle", new EllipseOverlay(x, y, r, r, phi));
            data.put(outputKey + "circle_sigma1", new EllipseOverlay(x, y, r - sigma, r - sigma, phi));
            data.put(outputKey + "circle_sigma2", new EllipseOverlay(x, y, r + sigma, r + sigma, phi));
        } else {
            data.put(outputKey + "circle", new EllipseOverlay(500, 0, 1, 1, 0));
            data.put(outputKey + "circle_sigma1", new EllipseOverlay(500, 0, 1, 1, 0));
            data.put(outputKey + "circle_sigma2", new EllipseOverlay(500, 0, 1, 1, 0));
        }

        return data;
    }

    public class LightDistributionNegLogLikelihood implements MultivariateFunction {
        private double[] photoncharge;
        private int[] cleaning_pixel;
        private FactPixelMapping pixelMapping = FactPixelMapping.getInstance();
        private double mirror_radius = 1.85;
        private double pixel_fov = Math.toRadians(0.11);
        private double focal_length = 4889;  // in mm
        // int_{lambda_1}^{lambda_2} lambda^{-2} dlambda
        private double integral = 2222222.22;

        /**
         * @param photoncharge   double array containing the photoncharge for each pixel
         * @param cleaning_pixel int array containing all pixel chids for the pixel that survived cleaning
         */
        public LightDistributionNegLogLikelihood(double[] photoncharge, int[] cleaning_pixel) {
            this.photoncharge = photoncharge;
            this.cleaning_pixel = cleaning_pixel;
        }

        /**
         * @param point a double array with length 4 containing r, x, y, sigma in this order
         * @return the negative log likelihood at this point for the given data
         */
        public double value(double[] point) {
            double r = point[0];
            double x = point[1];
            double y = point[2];
            double sigma = point[3];
            double rho = point[4];
            double phi = point[5];
            double eps = point[6];
            double ln_L = 0;

            for (int i = 0; i < cleaning_pixel.length; i++) {
                int pix = cleaning_pixel[i];
                double photons = photon_expectance_pixel(pix, r, x, y, sigma, rho, phi, eps);
                ln_L += PoissonLogP(photons, photoncharge[pix]);
            }

            return -ln_L;
        }

        public double gauss_density(double x, double mean, double std) {
            double norm = 1.0 / Math.sqrt(2.0 * Math.PI * Math.pow(std, 2.0));
            return norm * Math.exp(-0.5 * Math.pow((x - mean) / std, 2.0));
        }

        private double intensity(double phi, double ring_radius, double rho, double eps) {
            double theta_c = radius2theta(ring_radius);
            double D;
            if (rho > mirror_radius) {
                if (Math.abs(phi) < Math.asin(mirror_radius / rho)) {
                    D = 2 * mirror_radius * Math.sqrt(1 - Math.pow((rho / mirror_radius) * Math.sin(phi), 2.0));
                } else {
                    D = 0;
                }
            } else {
                D = Math.sqrt(1 - Math.pow(rho / mirror_radius * Math.sin(phi), 2.0));
                D += rho / mirror_radius * Math.cos(phi);
                D *= mirror_radius;
            }

            return eps * 1.0 / (137 * 2.0) * integral * (pixel_fov / theta_c) * Math.sin(2.0 * theta_c) * D;
        }

        public double radius2theta(double radius) {
            return radius / focal_length;
        }

        public double photon_expectance_pixel(int chid, double r, double x, double y, double sigma, double rho, double phi, double eps) {
            double pixel_x = pixelMapping.getPixelFromId(chid).getXPositionInMM();
            double pixel_y = pixelMapping.getPixelFromId(chid).getYPositionInMM();
            double pixel_phi = Math.atan2(pixel_y - y, pixel_x - x);
            double pixel_r = Math.sqrt(Math.pow(pixel_x - x, 2.0) + Math.pow(pixel_y - y, 2.0));
            return intensity(pixel_phi - phi, r, rho, eps) * Constants.PIXEL_SIZE_MM * gauss_density(pixel_r, r, sigma);
        }

        private double PoissonLogP(double lambda, double k) {
            return k * Math.log(lambda) - lambda;
        }
    }
}
