package fact.datacorrection;

import fact.Utils;
import fact.calibrationservice.CalibrationService;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 *
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
  * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class InterpolatePixelArray implements Processor {

    static Logger log = LoggerFactory.getLogger(InterpolatePixelArray.class);

    @Service(required = true, description = "The calibration service which provides the information about the bad pixels")
    CalibrationService calibService;

    @Parameter(required = true, description = "The photoncharge key to work on")
    private String inputKey = null;

    @Parameter(required = true, description = "The name of the interpolated photoncharge output")
    private String outputKey = null;

    @Parameter(description = "The minimum number of neighboring pixels required for interpolation", defaultValue="3")
    private int minPixelToInterpolate = 3;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();


    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, inputKey, double[].class);

        double[] input = (double[]) item.get(inputKey);

        ZonedDateTime timeStamp;

        if (item.containsKey("UnixTimeUTC") == true) {
            Utils.isKeyValid(item, "UnixTimeUTC", int[].class);
            int[] eventTime = (int[]) item.get("UnixTimeUTC");
            timeStamp = Utils.unixTimeUTCToZonedDateTime(eventTime);
        } else {
            // MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
            // => The 12 bad pixels we have from the beginning on are used.
            timeStamp = ZonedDateTime.of(2000, 1, 1, 0, 0,0,0, ZoneOffset.UTC);
        }

        PixelSet badPixelsSet = calibService.getBadPixel(timeStamp);

        double[] output;
        if (!inputKey.equals(outputKey)) {
            output = new double[input.length];
            System.arraycopy(input,0, output, 0, input.length);
        } else {
            output = input;
        }
        output = interpolatePixelArray(output, badPixelsSet);
        item.put(outputKey, output);
        return item;
    }

    double[] interpolatePixelArray(double[] pixelArray, PixelSet badPixels) {
        for (CameraPixel pixel: badPixels){
            CameraPixel[] currentNeighbors = pixelMap.getNeighborsForPixel(pixel);
            double avg = 0.0;
            int numNeighbours = 0;
            for (CameraPixel neighbor: currentNeighbors){
                if (badPixels.contains(neighbor)) {
                    continue;
                }
                avg += pixelArray[neighbor.id];
                numNeighbours++;
            }
            checkNumNeighbours(numNeighbours, pixel.id);
            pixelArray[pixel.id] = avg / numNeighbours;
        }
        return pixelArray;
    }

    void checkNumNeighbours(int numNeighbours, int pixToInterpolate) {
        if (numNeighbours == 0) {
            throw new RuntimeException("A pixel (chid: "+ pixToInterpolate + ") shall be interpolated, but there a no valid "
                    + "neighboring pixel to interpolate.");
        }
        if (numNeighbours < minPixelToInterpolate) {
            throw new RuntimeException("A pixel (chid: "+ pixToInterpolate + ") shall be interpolated, but there are only "
                    + numNeighbours + " valid neighboring pixel to interpolate.\n" +
                    "Minimum number of pixel to interpolate is set to " + minPixelToInterpolate);
        }
    }
}
