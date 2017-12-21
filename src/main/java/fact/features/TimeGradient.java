package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.CameraPixel;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the timing gradient along the longitudinal and the transversal axis of the shower.
 * Therefore the ellipse coordinates ({l,t}) are calculated and the function arrTime(l)=s_l*l + i_l
 * and arrTime(t)=s_t*t + i_t are fitted.
 * The parameters (s_l,i_l,s_t,i_t) of the fits, their standard deviation and the sum squared errors of the fits are pushed in the
 * data item.
 *
 * @author Fabian Temme
 */
public class TimeGradient implements Processor {

    static Logger log = LoggerFactory.getLogger(TimeGradient.class);

    @Parameter(required = true, description = "key to the shower pixels")
    public String pixelSetKey = null;

    @Parameter(required = true, description = "key to the arrival times of all pixels")
    public String arrivalTimeKey = null;

    @Parameter(required = true, description = "key to the xvalue of the cog of the shower")
    public String cogKey = null;

    @Parameter(required = true, description = "key to the delta angle of the shower")
    public String deltaKey = null;

    @Parameter(required = true, description = "outputKey for the calculated timegradient slopes")
    public String outputKeySlope = null;

    @Parameter(required = true, description = "outputKey for the calculated timegradient intercepts")
    public String outputKeyIntercept = null;

    @Parameter(required = true, description = "outputKey for the sum squared errors of the linear fits")
    public String outputKeySumSquaredErrors = null;

    public Data process(Data input) {

        Utils.mapContainsKeys(input, pixelSetKey, arrivalTimeKey, cogKey, deltaKey);
        Utils.isKeyValid(input, pixelSetKey, PixelSet.class);
        Utils.isKeyValid(input, cogKey, CameraCoordinate.class);

        PixelSet shower = (PixelSet) input.get(pixelSetKey);
        double[] arrivalTime = (double[]) input.get(arrivalTimeKey);
        double delta = (Double) input.get(deltaKey);
        CameraCoordinate cog = (CameraCoordinate) input.get(cogKey);

        SimpleRegression regressorLong = new SimpleRegression();
        SimpleRegression regressorTrans = new SimpleRegression();

        for (CameraPixel px : shower.set) {
            double x = px.getXPositionInMM();
            double y = px.getYPositionInMM();
            double[] ellipseCoord = Utils.transformToEllipseCoordinates(x, y, cog.xMM, cog.yMM, delta);
            double time = arrivalTime[px.id];
            regressorLong.addData(ellipseCoord[0], time);
            regressorTrans.addData(ellipseCoord[1], time);
        }

        double[] slope = {0, 0};
        double[] slopeErr = {0, 0};
        double[] intercept = {0, 0};
        double[] interceptErr = {0, 0};
        double[] sumSquaredErrors = {0, 0};

        try {
            regressorLong.regress();
            slope[0] = regressorLong.getSlope();
            slopeErr[0] = regressorLong.getSlopeStdErr();
            intercept[0] = regressorLong.getIntercept();
            interceptErr[0] = regressorLong.getInterceptStdErr();
            sumSquaredErrors[0] = regressorLong.getSumSquaredErrors();
        } catch (NoDataException exc) {
            log.warn("Not enough data points to regress the longitudinal timegradient. Putting Double.NaN in data item");
            slope[0] = Double.NaN;
            slopeErr[0] = Double.NaN;
            intercept[0] = Double.NaN;
            interceptErr[0] = Double.NaN;
            sumSquaredErrors[0] = Double.NaN;
        }

        try {
            regressorTrans.regress();
            slope[1] = regressorTrans.getSlope();
            slopeErr[1] = regressorTrans.getSlopeStdErr();
            intercept[1] = regressorTrans.getIntercept();
            interceptErr[1] = regressorTrans.getInterceptStdErr();
            sumSquaredErrors[1] = regressorTrans.getSumSquaredErrors();
        } catch (NoDataException exc) {
            log.warn("Not enough data points to regress the transversal timegradient. Putting Double.NaN in data item");
            slope[0] = Double.NaN;
            slopeErr[0] = Double.NaN;
            intercept[0] = Double.NaN;
            interceptErr[0] = Double.NaN;
            sumSquaredErrors[0] = Double.NaN;
        }

        input.put(outputKeySlope, slope);
        input.put(outputKeySlope + "_err", slopeErr);
        input.put(outputKeyIntercept, intercept);
        input.put(outputKeyIntercept + "_err", interceptErr);
        input.put(outputKeySumSquaredErrors, sumSquaredErrors);

        return input;
    }
}
