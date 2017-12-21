package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;

/**
 * This processor takes the calculated time gradient along the longitudinal axis of a shower, calculated by the standard preprocessing.
 * Using this time gradient it predicts a time for the cherenkov pulse in the data array, individual for each pixel, depending on the
 * longitudinal coordinate of the pixel.
 * Around this predicted time, the standard algorithm for calculating the photoncharge (CalculateMaxPosition(),CalculatePositionHalfHeight(),
 * CalculateIntegral()) are applied to calculate a new photoncharge.
 *
 * @author Fabian Temme
 */
public class TimeGradientExtraction extends BasicExtraction {
    static Logger log = LoggerFactory.getLogger(TimeGradientExtraction.class);

    @Parameter(required = true, description = "key to the delta angle of the shower")
    public String deltaKey = null;

    @Parameter(required = true, description = "key to the xvalue of the cog of the shower")
    public String cogxKey = null;

    @Parameter(required = true, description = "key to the yvalue of the cog of the shower")
    public String cogyKey = null;

    @Parameter(required = true, description = "key to the timegradient slopes")
    public String timeGradientSlopeKey = null;

    @Parameter(required = true, description = "key to the timegradient intercepts")
    public String timeGradientInterceptKey = null;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data input) {

        Utils.mapContainsKeys(input, deltaKey, cogxKey, cogyKey, dataKey, timeGradientSlopeKey, timeGradientInterceptKey);

        double[] data = (double[]) input.get(dataKey);
        double delta = (Double) input.get(deltaKey);
        double cogx = (Double) input.get(cogxKey);
        double cogy = (Double) input.get(cogyKey);
        double[] slopes = (double[]) input.get(timeGradientSlopeKey);
        double[] intercepts = (double[]) input.get(timeGradientInterceptKey);
        int roi = (Integer) input.get("NROI");

        int[] positions = new int[Constants.NUMBEROFPIXEL];
        IntervalMarker[] mPositions = new IntervalMarker[Constants.NUMBEROFPIXEL];
        double[] photonCharge = new double[Constants.NUMBEROFPIXEL];
        IntervalMarker[] mPhotonCharge = new IntervalMarker[Constants.NUMBEROFPIXEL];

        for (int px = 0; px < Constants.NUMBEROFPIXEL; px++) {
            double x = pixelMap.getPixelFromId(px).getXPositionInMM();
            double y = pixelMap.getPixelFromId(px).getYPositionInMM();
            double[] ellipseCoord = Utils.transformToEllipseCoordinates(x, y, cogx, cogy, delta);

            double predictedTime = slopes[0] * ellipseCoord[0] + intercepts[0];
            int predictedSlice = (int) Math.round(predictedTime);

            int leftBorder = predictedSlice - rangeSearchWindow / 2;
            int[] window = Utils.getValidWindow(leftBorder, rangeSearchWindow, rangeHalfHeightWindow + validMinimalSlice, 210);

            positions[px] = calculateMaxPosition(px, window[0], window[1], roi, data);
            mPositions[px] = new IntervalMarker(positions[px], positions[px] + 1);

            int halfHeightPos = calculatePositionHalfHeight(px, positions[px], positions[px] - rangeHalfHeightWindow, roi, data);

            Utils.checkWindow(halfHeightPos, integrationWindow, validMinimalSlice, roi);
            photonCharge[px] = calculateIntegral(px, halfHeightPos, integrationWindow, roi, data) / integralGains[px];
            mPhotonCharge[px] = new IntervalMarker(halfHeightPos, halfHeightPos + integrationWindow);
        }

        input.put(outputKeyMaxAmplPos, positions);
        input.put(outputKeyMaxAmplPos + "Marker", mPositions);
        input.put(outputKeyPhotonCharge, photonCharge);
        input.put("@photoncharge", photonCharge);
        input.put(outputKeyPhotonCharge + "Marker", mPhotonCharge);

        return input;
    }
}
