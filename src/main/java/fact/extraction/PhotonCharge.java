package fact.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;

/**
 * This processor computes the photon charge based on the maximum amplitudes and
 * the data array. The maximum amplitudes need to be provided as an array,
 * holding the position (slice) of the maximum for each pixel.
 * 
 * The photon charge is computed by integration of a window of slices. This
 * window is derived from the maximum amplitude position and the position of the
 * half-value of the maximum before the maximum. The resulting sum is divided by
 * the integralGain for each pixel, leaving the photoncharge value.
 * 
 * 
 * @author Fabian Temme, adapted by Christian Bockermann
 *
 */
public class PhotonCharge implements Processor {
    static Logger log = LoggerFactory.getLogger(PhotonCharge.class);

    @Parameter(required = true, description = "key to the data array")
    protected String dataKey = "data:calibrated";

    @Parameter(required = true, description = "outputKey for the calculated photoncharge")
    protected String outputKeyPhotonCharge = null;

    @Parameter(description = "Attribute holding the maximum positions of all amplitudes, default 'amplitudes:max:pos'.")
    String amplitudePosKey = "amplitudes:max:pos";

    @Parameter(required = false, description = "The url to the inputfiles for the gain calibration constants", defaultValue = "file:src/main/resources/defaultIntegralGains.csv")
    protected SourceURL url = null;

    @Parameter(required = false, description = "range of the search window for the half heigt position", defaultValue = "25")
    protected int rangeHalfHeightWindow = 25;

    @Parameter(required = false, description = "range of the integration window", defaultValue = "30")
    protected int integrationWindow = 30;

    @Parameter(required = false, description = "minimal slice with valid values (we want to ignore slices below this value", defaultValue = "10")
    protected int validMinimalSlice = 10;

    protected double[] integralGains = null;

    private int npix = Constants.NUMBEROFPIXEL;

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, dataKey, "NROI");

        int roi = (Integer) input.get("NROI");
        int npix = (Integer) input.get("NPIX");

        final double[] data = (double[]) input.get(dataKey);

        final int[] positions = (int[]) input.get(amplitudePosKey);
        final double[] photonCharge = new double[npix];

        for (int pix = 0; pix < npix; pix++) {

            int halfHeightPos = calculatePositionHalfHeight(pix, positions[pix],
                    Math.max(0, positions[pix] - rangeHalfHeightWindow), roi, data);

            int start = Math.min(Math.max(validMinimalSlice, halfHeightPos), roi);
            photonCharge[pix] = calculateIntegral(pix, start, integrationWindow, roi, data) / integralGains[pix];
        }

        input.put(outputKeyPhotonCharge, photonCharge);
        return input;
    }

    /**
     * In an area ]amplitudePositon-leftBorder,amplitudePosition] searches for
     * the last position, where data[pos] is < 0.5 * maxAmplitude. Returns the
     * following slice.
     * 
     * @param px
     * @param maxPos
     * @param leftBorder
     * @param roi
     * @param data
     * @return
     */
    private int calculatePositionHalfHeight(int px, int maxPos, int leftBorder, int roi, double[] data) {
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

    private double calculateIntegral(int px, int startingPosition, int integralSize, int roi, double[] data) {
        double integral = 0;
        for (int sl = startingPosition; sl < startingPosition + integralSize; sl++) {
            int pos = px * roi + sl;
            integral += data[pos];
        }
        return integral;
    }

    private double[] loadIntegralGainFile(SourceURL inputUrl, Logger log) {
        double[] integralGains = new double[npix];
        Data integralGainData = null;
        try {
            CsvStream stream = new CsvStream(inputUrl, " ");
            stream.setHeader(false);
            stream.init();
            integralGainData = stream.readNext();

            for (int i = 0; i < npix; i++) {
                String key = "column:" + (i);
                integralGains[i] = (Double) integralGainData.get(key);
            }
            return integralGains;

        } catch (Exception e) {
            log.error("Failed to load integral Gain data: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void setUrl(SourceURL url) {
        try {
            integralGains = loadIntegralGainFile(url, log);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        this.url = url;
    }
}