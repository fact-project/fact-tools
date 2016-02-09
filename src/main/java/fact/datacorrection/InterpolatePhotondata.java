package fact.datacorrection;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Utils;
import fact.calibrationservice.CalibrationService;
import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

/**
 *
 * This Processor interpolates all values for a broken Pixel by the average
 * values of its neighboring Pixels.
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class InterpolatePhotondata implements Processor {
    static Logger log = LoggerFactory.getLogger(InterpolatePhotondata.class);

    @Service(description = "The calibration service which provides the information about the bad pixels")
    CalibrationService calibService;

    @Parameter(required = true, description = "The photoncharge key to work on", defaultValue="pixels:estNumPhotons")
    private String estNumPhotonsKey = "pixels:estNumPhotons";
    @Parameter(required = true, description = "The name of the interpolated photoncharge output", defaultValue="pixels:estNumPhotons")
    private String estNumPhotonsOutputKey = "pixels:estNumPhotons";
    @Parameter(required = true, description = "The arrivalTime key to work on", defaultValue="pixels:arrivalTimes")
    private String arrivalTimesKey = "pixels:arrivalTimes";
    @Parameter(required = true, description = "The name of the interpolated arrivalTime output", defaultValue="pixels:arrivalTimes")
    private String arrivalTimesOutputKey = "pixels:arrivalTimes";
    @Parameter(required = false, description = "The minimum number of neighboring pixels required for interpolation", defaultValue = "3")
    private int minPixelToInterpolate = 3;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, estNumPhotonsKey, double[].class);
        Utils.isKeyValid(item, arrivalTimesKey, double[].class);
        double[] estNumPhotons = (double[]) item.get(estNumPhotonsKey);
        double[] arrivalTimes = (double[]) item.get(arrivalTimesKey);

        DateTime timeStamp = null;

        if (item.containsKey("UnixTimeUTC") == true) {
            Utils.isKeyValid(item, "UnixTimeUTC", int[].class);
            int[] eventTime = (int[]) item.get("UnixTimeUTC");
            timeStamp = new DateTime((long) ((eventTime[0] + eventTime[1] / 1000000.) * 1000), DateTimeZone.UTC);
        } else {
            // MC Files don't have a UnixTimeUTC in the data item. Here the
            // timestamp is hardcoded to 1.1.2000
            // => The 12 bad pixels we have from the beginning on are used.
            timeStamp = new DateTime(2000, 1, 1, 0, 0);
        }

        int[] badChIds = calibService.getBadPixel(timeStamp);
        PixelSet badPixelsSet = new PixelSet();
        for (int px : badChIds) {
            badPixelsSet.addById(px);
        }

        if (!estNumPhotonsKey.equals(estNumPhotonsOutputKey)) {
            double[] newEstNumPhotons = new double[estNumPhotons.length];
            System.arraycopy(estNumPhotons, 0, newEstNumPhotons, 0, estNumPhotons.length);
            estNumPhotons = interpolatePixelArray(newEstNumPhotons, badChIds);
        } else {
            estNumPhotons = interpolatePixelArray(estNumPhotons, badChIds);
        }
        if (!arrivalTimesKey.equals(arrivalTimesOutputKey)) {
            double[] newArrivalTimes = new double[arrivalTimes.length];
            System.arraycopy(arrivalTimes, 0, newArrivalTimes, 0, arrivalTimes.length);
            arrivalTimes = interpolatePixelArray(newArrivalTimes, badChIds);
        } else {
            arrivalTimes = interpolatePixelArray(arrivalTimes, badChIds);
        }
        item.put(estNumPhotonsOutputKey, estNumPhotons);
        item.put(arrivalTimesOutputKey, arrivalTimes);
        item.put("Bad pixels", badPixelsSet);

        return item;
    }

    private double[] interpolatePixelArray(double[] pixelArray, int[] badChIds) {
        for (int pix : badChIds) {
            FactCameraPixel[] currentNeighbors = pixelMap.getNeighboursFromID(pix);
            double avg = 0.0f;
            int numNeighbours = 0;
            for (FactCameraPixel nPix : currentNeighbors) {
                if (ArrayUtils.contains(badChIds, nPix.id)) {
                    continue;
                }
                avg += pixelArray[nPix.id];
                numNeighbours++;
            }
            checkNumNeighbours(numNeighbours, pix);
            pixelArray[pix] = avg / numNeighbours;
        }
        return pixelArray;
    }

    private void checkNumNeighbours(int numNeighbours, int pixToInterpolate) {
        if (numNeighbours == 0) {
            throw new RuntimeException("A pixel (chid: " + pixToInterpolate
                    + ") shall be interpolated, but there a no valid " + "neighboring pixel to interpolate.");
        }
        if (numNeighbours < minPixelToInterpolate) {
            throw new RuntimeException(
                    "A pixel (chid: " + pixToInterpolate + ") shall be interpolated, but there are only "
                            + numNeighbours + " valid neighboring pixel to interpolate.\n"
                            + "Minimum number of pixel to interpolate is set to " + minPixelToInterpolate);
        }
    }

}
