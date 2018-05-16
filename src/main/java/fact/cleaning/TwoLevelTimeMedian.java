package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.CameraPixel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * TwoLevelTimeMedian. Identifies showerPixel in the image array.
 * Cleaning in three Steps:
 * 1) Identify all Core Pixel (Photoncharge higher than corePixelThreshold)
 * 2) Remove all Single Core Pixel
 * 3) Add all Neighbor Pixel, whose Photoncharge is higher than neighborPixelThreshold
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 */

public class TwoLevelTimeMedian extends BasicCleaning implements Processor {
    static Logger log = LoggerFactory.getLogger(TwoLevelTimeMedian.class);

    @Parameter(required = true)
    public String photonChargeKey;

    @Parameter(required = true)
    public String arrivalTimeKey;

    @Parameter(required = true)
    public String outputKey;

    @Parameter(required = true, description = "The smallest PhotonCharge a Pixel must have to be " +
            "identified as a CorePixel")
    public double corePixelThreshold;

    @Parameter(required = true, description = "The smallest PhotonCharge a Pixel must have that is adjacent to a " +
            "previously identified corePixel")
    public double neighborPixelThreshold;

    @Parameter(required = true, description = "Maximal difference in arrival time to the median of the arrival times of the shower" +
            ", which a pixel is alound to have after cleaning")
    public double timeLimit;

    @Parameter(required = true, description = "Number of Pixels a patch of CorePixel must have before its Neighbours" +
            " are even considered for NeighbourCorePixel. " +
            " If Size is smaller than minSize the Pixels will be discarded.")
    public int minNumberOfPixel;

    @Parameter(required = false)
    public String[] starPositionKeys = null;

    @Parameter(required = false, defaultValue = "Constants.PIXEL_SIZE_MM")
    public double starRadiusInCamera = Constants.PIXEL_SIZE_MM;

    @Parameter
    public boolean showDifferentCleaningSets = true;

    @Override
    public Data process(Data item) {

        ZonedDateTime timeStamp = Utils.getTimeStamp(item);

        double[] photonCharge = Utils.toDoubleArray(item.get(photonChargeKey));
        double[] arrivalTimes = Utils.toDoubleArray(item.get(arrivalTimeKey));

        PixelSet showerPixel = new PixelSet();

        showerPixel = addCorePixel(showerPixel, photonCharge, corePixelThreshold, timeStamp);
        if (showDifferentCleaningSets == true) {
            addLevelToDataItem(showerPixel, outputKey + "_level1", item);
        }

        showerPixel = removeSmallCluster(showerPixel, minNumberOfPixel);
        if (showDifferentCleaningSets == true) {
            addLevelToDataItem(showerPixel, outputKey + "_level2", item);
        }

        showerPixel = addNeighboringPixels(showerPixel, photonCharge, neighborPixelThreshold, timeStamp);
        if (showDifferentCleaningSets == true) {
            addLevelToDataItem(showerPixel, outputKey + "_level3", item);
        }

        if (notUsablePixelSet != null) {
            item.put("notUsablePixelSet", notUsablePixelSet);
        }

        //in case we have no showerpixels. We wont get any new ones in the steps below. And also it would crash.
        if (showerPixel.size() == 0) {
            return item;
        }

        // Hacky method to increase the timeLimit for larger showers (which could have a larger spread in the arrival times):
        double currentTimeThreshold = timeLimit;
        if (showerPixel.size() > 50) {
            currentTimeThreshold = timeLimit * Math.log10(showerPixel.size());
        }

        showerPixel = applyTimeMedianCleaning(showerPixel, arrivalTimes, currentTimeThreshold);
        if (showDifferentCleaningSets == true) {
            addLevelToDataItem(showerPixel, outputKey + "_level4", item);
        }

        showerPixel = removeSmallCluster(showerPixel, minNumberOfPixel);
        if (showDifferentCleaningSets == true) {
            addLevelToDataItem(showerPixel, outputKey + "_level5", item);
        }

        if (starPositionKeys != null) {
            PixelSet starSet = new PixelSet();
            for (String starPositionKey : starPositionKeys) {
                Utils.isKeyValid(item, starPositionKey, CameraCoordinate.class);
                CameraCoordinate starPosition = (CameraCoordinate) item.get(starPositionKey);

                showerPixel = removeStarIslands(showerPixel, starPosition, starSet, starRadiusInCamera, log);
                if (showDifferentCleaningSets == true) {
                    addLevelToDataItem(showerPixel, outputKey + "_level6", item);
                    item.put("Starset", starSet);
                }
            }
        }

        item.put(outputKey, showerPixel);

        return item;
    }

    /**
     * Remove pixels with a difference in the arrivalTime to the median of the arrivalTimes of all pixels, larger than the timeLimit
     *
     * @param showerPixel
     * @param arrivalTime
     * @param timeThreshold
     * @return
     */
    public PixelSet applyTimeMedianCleaning(PixelSet showerPixel, double[] arrivalTime, double timeThreshold) {

        double[] showerArrivals = new double[showerPixel.size()];
        int i = 0;
        for (CameraPixel pixel : showerPixel) {
            showerArrivals[i] = arrivalTime[pixel.id];
            i++;
        }
        double median = calculateMedian(showerArrivals);

        PixelSet newShowerPixel = new PixelSet();
        for (CameraPixel pixel : showerPixel) {
            if (Math.abs(arrivalTime[pixel.id] - median) < timeThreshold) {
                newShowerPixel.add(pixel);
            }
        }
        return newShowerPixel;
    }


    private double calculateMedian(double[] showerArrivals) {
        double median = 0.0;
        Arrays.sort(showerArrivals);
        int length = showerArrivals.length;
        if (showerArrivals.length % 2 == 1) {
            median = showerArrivals[(length - 1) / 2];
        } else {
            median = 0.5 * (showerArrivals[(length) / 2] + showerArrivals[(length) / 2 - 1]);
        }
        return median;
    }

}
