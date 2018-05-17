package fact.datacorrection;

import fact.Constants;
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
 * It will interpolate the pixels from the GainService.badPixels pixelset as well as all pixels having not-finite
 * values in the input array.
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class InterpolatePixelArray implements Processor {

    static Logger log = LoggerFactory.getLogger(InterpolatePixelArray.class);

    @Service(required = true, description = "The calibration service which provides the information about the bad pixels")
    public CalibrationService calibService;

    @Parameter(required = true, description = "The photoncharge key to work on")
    public String inputKey = null;

    @Parameter(required = true, description = "The name of the interpolated photoncharge output")
    public String outputKey = null;

    @Parameter(description = "The minimum number of neighboring pixels required for interpolation", defaultValue="3")
    public int minPixelToInterpolate = 3;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();


    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, inputKey, double[].class);

        double[] input = (double[]) item.get(inputKey);

        ZonedDateTime timeStamp = Utils.getTimeStamp(item);
        PixelSet badPixelsSet = calibService.getBadPixels(timeStamp);

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
        for (int chid = 0; chid < Constants.N_PIXELS; chid++){
            if (badPixels.containsID(chid) || (!Double.isFinite(pixelArray[chid]))) {
                CameraPixel[] currentNeighbors = pixelMap.getNeighborsFromID(chid);

                double avg = 0.0;
                int numNeighbours = 0;
                for (CameraPixel neighbor: currentNeighbors){
                    if (badPixels.contains(neighbor) || (!Double.isFinite(pixelArray[neighbor.id]))) {
                        continue;
                    }
                    avg += pixelArray[neighbor.id];
                    numNeighbours++;
                }
                checkNumNeighbours(numNeighbours, chid);
                pixelArray[chid] = avg / numNeighbours;
            }
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
