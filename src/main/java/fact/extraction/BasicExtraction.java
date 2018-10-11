package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.gainservice.GainService;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.time.ZonedDateTime;

/**
 * This processor performs a basic extraction on the data array. It contains three steps:
 * 1. Calculates the position of the max amplitude in [startSearchWindow,startSearchWindow+rangeSearchWindow[
 * 2. Calculates the position of the half height in front of the maxAmplitudePosition
 * 3. Calculates the integral by summing up the following integrationWindow slices beginning with the half heigth position
 * The resulting photoncharge is calculated by dividing the integral by the integralGain of the pixel
 * <p>
 * This processor also serves as a basic class for extraction processors
 *
 * @author Fabian Temme
 */
public class BasicExtraction implements Processor {
    private static final Logger log = LoggerFactory.getLogger(BasicExtraction.class);

    @Parameter(required = true, description = "key to the data array")
    public String dataKey = null;

    @Parameter(required = true, description = "outputKey for the position of the max amplitudes")
    public String outputKeyMaxAmplPos = null;

    @Parameter(required = true, description = "outputKey for the calculated photoncharge")
    public String outputKeyPhotonCharge = null;

    @Service(description = "Gain Service that delivers the integral gains")
    public GainService gainService = null;

    @Parameter(required = false, description = "start slice of the search window for the max amplitude", defaultValue = "35")
    public int startSearchWindow = 35;

    @Parameter(required = false, description = "range of the search window for the max amplitude", defaultValue = "90")
    public int rangeSearchWindow = 90;

    @Parameter(required = false, description = "range of the search window for the half heigt position", defaultValue = "25")
    public int rangeHalfHeightWindow = 25;

    @Parameter(required = false, description = "range of the integration window", defaultValue = "30")
    public int integrationWindow = 30;

    @Parameter(required = false, description = "minimal slice with valid values (we want to ignore slices below this value", defaultValue = "10")
    public int validMinimalSlice = 10;


    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, dataKey, "NROI");

        double[] integralGains;
        if (!item.containsKey("timestamp")) {
            integralGains = gainService.getSimulationGains();
        } else {
            ZonedDateTime timestamp = Utils.getTimeStamp(item);
            integralGains = gainService.getGains(timestamp);
        }

        int roi = (Integer) item.get("NROI");

        double[] data = (double[]) item.get(dataKey);

        int[] positions = new int[Constants.N_PIXELS];
        IntervalMarker[] mPositions = new IntervalMarker[Constants.N_PIXELS];
        double[] photonCharge = new double[Constants.N_PIXELS];
        IntervalMarker[] mPhotonCharge = new IntervalMarker[Constants.N_PIXELS];

        Utils.checkWindow(startSearchWindow, rangeSearchWindow, rangeHalfHeightWindow + validMinimalSlice, roi);

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            positions[pix] = calculateMaxPosition(pix, startSearchWindow, startSearchWindow + rangeSearchWindow, roi, data);
            mPositions[pix] = new IntervalMarker(positions[pix], positions[pix] + 1);

            int halfHeightPos = calculatePositionHalfHeight(pix, positions[pix], positions[pix] - rangeHalfHeightWindow, roi, data);

            Utils.checkWindow(halfHeightPos, integrationWindow, validMinimalSlice, roi);
            photonCharge[pix] = calculateIntegral(pix, halfHeightPos, integrationWindow, roi, data) / integralGains[pix];
            mPhotonCharge[pix] = new IntervalMarker(halfHeightPos, halfHeightPos + integrationWindow);
        }
        item.put(outputKeyMaxAmplPos, positions);
        item.put(outputKeyMaxAmplPos + "Marker", mPositions);
        item.put(outputKeyPhotonCharge, photonCharge);
        item.put("@photoncharge", photonCharge);
        item.put(outputKeyPhotonCharge + "Marker", mPhotonCharge);

        return item;
    }

    public int calculateMaxPosition(int px, int start, int rightBorder, int roi, double[] data) {
        int maxPos = start;
        double tempMax = -Double.MAX_VALUE;
        for (int sl = start; sl < rightBorder; sl++) {
            int pos = px * roi + sl;
            if (data[pos] > tempMax) {
                maxPos = sl;
                tempMax = data[pos];
            }
        }
        return maxPos;
    }

    /**
     * In an area ]amplitudePositon-leftBorder,amplitudePosition] searches for the last position, where data[pos] is < 0.5 *
     * maxAmplitude. Returns the following slice.
     *
     * @param px
     * @param maxPos
     * @param leftBorder
     * @param roi
     * @param data
     * @return
     */
    public int calculatePositionHalfHeight(int px, int maxPos, int leftBorder, int roi, double[] data) {
        int slice = maxPos;
        double maxHalf = data[px * roi + maxPos] / 2.0;
        for (; slice > leftBorder; slice--) {
            int pos = px * roi + slice;
            if (data[pos - 1] < maxHalf) {
                break;
            }
        }
        return slice;
    }

    public double calculateIntegral(int px, int startingPosition, int integralSize, int roi, double[] data) {
        double integral = 0;
        for (int sl = startingPosition; sl < startingPosition + integralSize; sl++) {
            int pos = px * roi + sl;
            integral += data[pos];
        }
        return integral;
    }
}
