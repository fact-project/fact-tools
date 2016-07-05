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
import fact.hexmap.ui.overlays.LineOverlay;

/*import fact.features.DistributionFromShower;*/


/**
 * Created by thomno on 13.06.2016
 */
public class mcdelta implements StatefulProcessor {
    /*
     * This Process calculate with a 2D Gaus new cov_ and new x and y.
     * With the new cov_ Array it calculate the new Delta, Length and width
     */
    @Parameter(required = false, description = "Key containing the photoncharges", defaultValue = "photoncharge")
    private String photonchargeKey = "photoncharge";
    @Parameter(required = false, description = "The pixelSet on which the fit is performed", defaultValue = "shower")
    private String pixelSetKey = "shower";
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the source coordinates")
    private String sourceZdKey = null;
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the source coordinates")
    private String sourceAzKey = null;
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the pointing coordinates")
    private String pointingZdKey = null;
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the pointing coordinates")
    private String pointingAzKey = null;
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the pointing coordinates")
    private String impactAzxKey = null;
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the pointing coordinates")
    private String impactAzyKey = null;

    @Parameter(required = false, description = "Base name for the output keys", defaultValue = "true_")
    private String outputKey = "true_";

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();


    @Override
    public Data process(Data data) {
        //double[] photoncharge = (double[]) data.get(photonchargeKey);
        double[] photoncharge = Utils.toDoubleArray(data.get(photonchargeKey));

        PixelSet pixelSet = (PixelSet) data.get(pixelSetKey);
        double[] showerWeights = createShowerWeights(pixelSet.toIntArray(),
            photoncharge);

        FactPixelMapping pixelMap = FactPixelMapping.getInstance();

        //Get Data
        double sourceZd = Utils.valueToDouble(data.get(sourceZdKey));
        double sourceAz = Utils.valueToDouble(data.get(sourceAzKey));
        double pointingZd = Utils.valueToDouble(data.get(pointingZdKey));
        double pointingAz = Utils.valueToDouble(data.get(pointingAzKey));
        double impactZd = Math.PI;
        double impactAzx = Utils.valueToDouble(data.get(impactAzxKey));
        double impactAzy = Utils.valueToDouble(data.get(impactAzyKey));

        //protonen
        //calcualate hypothetical x and y
        double pointingx_pro = 5;
        double pointingy_pro = Math.tan(Math.toRadians(pointingAz)) * pointingx_pro;

        //calculate Delta x and Delta y
        double x_p = Math.abs(pointingx_pro - impactAzx);
        double y_p = Math.abs(pointingy_pro - impactAzy);
        double deltamc_p = Math.atan(y_p / x_p);
        //delatmc is between 0 and pi/2

        //For the plot
        double linex1_p = pixelMap.getPixelFromId(393).getXPositionInMM();
        double liney1_p = pixelMap.getPixelFromId(393).getYPositionInMM();
        double r_p = Math.abs(linex1_p - pixelMap.getPixelFromId(357).getYPositionInMM());
        double rx_p = Math.cos(deltamc_p) * r_p;
        double ry_p = Math.sin(deltamc_p) * r_p;
        data.put("MCLinePro", new LineOverlay(linex1_p, liney1_p, linex1_p + rx_p, liney1_p + ry_p));

        data.put(outputKey + "deltapro", deltamc_p);

        //Gamma
        //calcualate hypothetical x and y
        double pointingx_gam = 5;
        double pointingy_gam = Math.tan(Math.toRadians(sourceAz)) * pointingx_gam;

        //calculate Delta x and Delta y
        double x_g = Math.abs(pointingx_gam - impactAzx);
        double y_g = Math.abs(pointingy_gam - impactAzy);
        double deltamc_g = Math.atan(y_g / x_g);
        //delatmc is between 0 and pi/2

        //For the plot
        double linex1_g = pixelMap.getPixelFromId(393).getXPositionInMM();
        double liney1_g = pixelMap.getPixelFromId(393).getYPositionInMM();
        double r_g = Math.abs(linex1_g - pixelMap.getPixelFromId(357).getYPositionInMM());
        double rx_g = Math.cos(deltamc_g) * r_g;
        double ry_g = Math.sin(deltamc_g) * r_g;
        data.put("MCLinegam", new LineOverlay(linex1_g, liney1_g, linex1_g + rx_g, liney1_g + ry_g));

        data.put(outputKey + "deltagam", deltamc_g);

        return data;
    }

    public void init(ProcessContext context) {
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

    public void setSourceZdKey(String sourceZdKey) {
        this.sourceZdKey = sourceZdKey;
    }

    public void setSourceAzKey(String sourceAzKey) {
        this.sourceAzKey = sourceAzKey;
    }

    public void setPointingZdKey(String pointingZdKey) {
        this.pointingZdKey = pointingZdKey;
    }

    public void setPointingAzKey(String pointingAzKey) {
        this.pointingAzKey = pointingAzKey;
    }

    public void setImpactAzxKey(String impactAzxKey) {
        this.impactAzxKey = impactAzxKey;
    }

    public void setImpactAzyKey(String impactAzyKey) {
        this.impactAzyKey = impactAzyKey;
    }

    public double[] createShowerWeights(int[] shower, double[] pixelWeights) {
        double[] weights = new double[shower.length];
        for (int i = 0; i < shower.length; i++) {
            weights[i] = pixelWeights[shower[i]];
        }
        return weights;
    }
}
