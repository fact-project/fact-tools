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

    @Parameter(description = "outputKey for the calculated timegradient slopes")
    public String outputKeySlope = "timeGradientSlope";

    @Parameter(description = "outputKey for the calculated timegradient intercepts")
    public String outputKeyIntercept = "timeGradientIntercept";

    @Parameter(description = "outputKey for the sum squared errors of the linear fits")
    public String outputKeySumSquaredErrors = "timeGradientSSE";

    public Data process(Data item) {

        Utils.mapContainsKeys(item, pixelSetKey, arrivalTimeKey, cogKey, deltaKey);
        Utils.isKeyValid(item, pixelSetKey, PixelSet.class);
        Utils.isKeyValid(item, cogKey, CameraCoordinate.class);

        PixelSet shower = (PixelSet) item.get(pixelSetKey);
        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);
        double delta = (Double) item.get(deltaKey);
        CameraCoordinate cog = (CameraCoordinate) item.get(cogKey);

        SimpleRegression regressionLong = new SimpleRegression();
        SimpleRegression regressionTrans = new SimpleRegression();

        for (CameraPixel px : shower) {
            double x = px.getXPositionInMM();
            double y = px.getYPositionInMM();
            double[] ellipseCoord = Utils.transformToEllipseCoordinates(x, y, cog.xMM, cog.yMM, delta);
            double time = arrivalTime[px.id];
            regressionLong.addData(ellipseCoord[0], time);
            regressionTrans.addData(ellipseCoord[1], time);
        }

        double slopeLong = Double.NaN;
        double slopeLongErr = Double.NaN;
        double interceptLong = Double.NaN;
        double interceptLongErr = Double.NaN;
        double sumSquaredErrorsLong = Double.NaN;
        try {
            regressionLong.regress();
            slopeLong = regressionLong.getSlope();
            slopeLongErr = regressionLong.getSlopeStdErr();
            interceptLong = regressionLong.getIntercept();
            interceptLongErr = regressionLong.getInterceptStdErr();
            sumSquaredErrorsLong = regressionLong.getSumSquaredErrors();
        } catch (NoDataException exc) {
            log.warn("Not enough data points to regress the longitudinal timegradient. Putting Double.NaN in data item");
        }

        regressionLong.regress();
        regressionLong.getSlope();

        double slopeTrans = Double.NaN;
        double slopeTransErr = Double.NaN;
        double interceptTrans = Double.NaN;
        double interceptTransErr = Double.NaN;
        double sumSquaredErrorsTrans = Double.NaN;
        try {
            regressionTrans.regress();
            slopeTrans = regressionTrans.getSlope();
            slopeTransErr = regressionTrans.getSlopeStdErr();
            interceptTrans = regressionTrans.getIntercept();
            interceptTransErr = regressionTrans.getInterceptStdErr();
            sumSquaredErrorsTrans = regressionTrans.getSumSquaredErrors();
        } catch (NoDataException exc) {
            log.warn("Not enough data points to regress the transverse timegradient. Putting Double.NaN in data item");
        }

        item.put(outputKeySlope + "_long", slopeLong);
        item.put(outputKeySlope + "_long_err", slopeLongErr);
        item.put(outputKeyIntercept + "_long", interceptLong);
        item.put(outputKeyIntercept + "_long_err", interceptLongErr);
        item.put(outputKeySumSquaredErrors + "_long", sumSquaredErrorsLong);

        item.put(outputKeySlope + "_trans", slopeTrans);
        item.put(outputKeySlope + "_trans_err", slopeTransErr);
        item.put(outputKeyIntercept + "_trans", interceptTrans);
        item.put(outputKeyIntercept + "_trans_err", interceptTransErr);
        item.put(outputKeySumSquaredErrors + "_trans", sumSquaredErrorsTrans);

        return item;
    }
}
