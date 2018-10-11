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
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class InterpolateTimeSeries implements Processor {
    static Logger log = LoggerFactory.getLogger(InterpolateTimeSeries.class);

    @Service(required = true, description = "The calibration service which provides the information about the bad pixels")
    public CalibrationService calibService;

    @Parameter(required = true, description = "The data key to work on")
    public String dataKey = null;

    @Parameter(required = true, description = "The name of the interpolated data output")
    public String dataOutputKey = null;

    @Parameter(required = false, description = "The minimum number of neighboring pixels required for interpolation", defaultValue = "3")
    public int minPixelToInterpolate = 3;

    @Parameter(required = false, description = "The key for the resulting badPixelSet.")
    public String badPixelKey = "badPixels";

    @Parameter(required = false, description = "The key to the timestamp of the Event.")
    public String timeStampKey = "timestamp";

    private FactPixelMapping pixelMap = FactPixelMapping.getInstance();


    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, dataKey, double[].class);
        double[] data = (double[]) item.get(dataKey);

        ZonedDateTime timeStamp = Utils.getTimeStamp(item, timeStampKey);

        PixelSet badPixelSet = calibService.getBadPixels(timeStamp);

        if (!dataKey.equals(dataOutputKey)) {
            double[] newdata = new double[data.length];
            System.arraycopy(data, 0, newdata, 0, data.length);
            data = interpolateTimeSeries(newdata, badPixelSet);
        } else {
            data = interpolateTimeSeries(data, badPixelSet);
        }

        item.put(dataOutputKey, data);
        item.put(badPixelKey, badPixelSet);
        return item;
    }

    public double[] interpolateTimeSeries(double[] data, PixelSet badPixelSet) {
        int roi = data.length / Constants.N_PIXELS;

        for (CameraPixel pixel : badPixelSet) {
            CameraPixel[] currentNeighbors = pixelMap.getNeighborsForPixel(pixel);

            //iterate over all slices
            for (int slice = 0; slice < roi; slice++) {
                int pos = pixel.id * roi + slice;
                //temp save the current value
                double avg = 0.0f;
                int numNeighbours = 0;

                for (CameraPixel neighborPixel : currentNeighbors) {
                    if (badPixelSet.contains(neighborPixel)) {
                        continue;
                    }
                    avg += data[neighborPixel.id * roi + slice];
                    numNeighbours++;
                }
                checkNumNeighbours(numNeighbours, pixel.id);
                //set value of current slice to average of surrounding pixels
                data[pos] = avg / (double) numNeighbours;
            }
        }
        return data;
    }

    private void checkNumNeighbours(int numNeighbours, int pixToInterpolate) {
        if (numNeighbours == 0) {
            throw new RuntimeException("A pixel (chid: " + pixToInterpolate + ") shall be interpolated, but there a no valid "
                    + "neighboring pixel to interpolate.");
        }
        if (numNeighbours < minPixelToInterpolate) {
            throw new RuntimeException("A pixel (chid: " + pixToInterpolate + ") shall be interpolated, but there are only "
                    + numNeighbours + " valid neighboring pixel to interpolate.\n" +
                    "Minimum number of pixel to interpolate is set to " + minPixelToInterpolate);
        }
    }
}
