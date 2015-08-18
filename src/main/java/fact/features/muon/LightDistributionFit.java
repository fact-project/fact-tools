package fact.features.muon;

import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
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
 * Created by maxnoe on 17.08.15.
 */
public class LightDistributionFit implements Processor {

    @Parameter(required = false, description = "key containing the photoncharges", defaultValue = "photoncharge")
    private String photonchargeKey = "photoncharge";
    @Parameter(required = false, description = "key containing the pixel that survided cleaning", defaultValue = "shower")
    private String cleaningPixelKey = "shower";
    @Parameter(required = false, description = "key containing the initial guess for the radius, if not given 100mm is used", defaultValue = "null")
    private String initialRKey = null;
    @Parameter(required = false, description = "key containing the initial guess for center x, if not given 0 is used", defaultValue = "null")
    private String initialXKey = null;
    @Parameter(required = false, description = "key containing the initial guess for center y, if not given 0 is used", defaultValue = "null")
    private String initialYKey = null;

    @Parameter(required = false, description = "outputkey basis, r,x,y,rho,phi,eps are appended to it", defaultValue = "muonfit_")
    private String outputKey = "muonfit_";

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
            for (int i = 0; i < result_point.length ; i++) {
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
        double[] test = new double[1440];
        for (int pix = 0; pix < 1440; pix++) {
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

    public void setPhotonchargeKey(String photonchargeKey) {
        this.photonchargeKey = photonchargeKey;
    }

    public void setInitialRKey(String initialRKey) {
        this.initialRKey = initialRKey;
    }

    public void setInitialXKey(String initialXKey) {
        this.initialXKey = initialXKey;
    }

    public void setInitialYKey(String initialYKey) {
        this.initialYKey = initialYKey;
    }

    public void setCleaningPixelKey(String cleaningPixelKey) {
        this.cleaningPixelKey = cleaningPixelKey;
    }

    public void setOutputKey(String outputKey) { this.outputKey = outputKey; }
}