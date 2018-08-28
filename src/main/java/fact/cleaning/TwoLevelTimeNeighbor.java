package fact.cleaning;


import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * TwoLevelTimeNeighbor. Identifies showerPixel in the image array.
 * Cleaning in several Steps:
 * 1) Identify all Core Pixel (Photoncharge higher than corePixelThreshold)
 * 2) Remove Small Cluster (Cluster with less than minNumberOfPixel Pixel)
 * 3) Add all Neighbor Pixel, whose Photoncharge is higher than neighborPixelThreshold
 * 4) Calculate for each Pixel the difference in arrival times to the neighboring Pixels. Remove all pixel
 * with less than 3 neighboring pixel with a difference smaller than timeLimit
 * 5) Remove Small Cluster (Cluster with less than minNumberOfPixel Pixel)
 * 6) Remove Star Cluster (Cluster which contains only pixel around a known star position
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 */

public class TwoLevelTimeNeighbor extends BasicCleaning implements Processor {
    static Logger log = LoggerFactory.getLogger(TwoLevelTimeNeighbor.class);

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
            ", which a pixel is aloud to have after cleaning")
    public double timeLimit;

    @Parameter(required = true, description = "Number of Pixels a patch of CorePixel must have before its Neighbours" +
            " are even considered for NeighbourCorePixel. " +
            " If Size is smaller than minSize the Pixels will be discarded.")
    public int minNumberOfPixel;


    @Parameter(required = false)
    public String[] starPositionKeys = null;

    @Parameter(required = false, defaultValue = "Constants.PIXEL_SIZE")
    public double starRadiusInCamera = Constants.PIXEL_SIZE_MM;

    @Parameter(description = "Add PixelSets for the different cleaning steps")
    public boolean showDifferentCleaningSets = false;


    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, arrivalTimeKey, double[].class);
        Utils.isKeyValid(item, photonChargeKey, double[].class);

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
            item.put(outputKey, new PixelSet());
            return item;
        }

        showerPixel = applyTimeNeighborCleaning(showerPixel, arrivalTimes, timeLimit, 2);
        if (showDifferentCleaningSets == true) {
            addLevelToDataItem(showerPixel, outputKey + "_level4", item);
        }

        showerPixel = removeSmallCluster(showerPixel, minNumberOfPixel);
        if (showDifferentCleaningSets == true) {
            addLevelToDataItem(showerPixel, outputKey + "_level5", item);
        }

        showerPixel = applyTimeNeighborCleaning(showerPixel, arrivalTimes, timeLimit, 1);
        if (showDifferentCleaningSets == true) {
            addLevelToDataItem(showerPixel, outputKey + "_level6", item);
        }

        if (starPositionKeys != null) {
            PixelSet starSet = new PixelSet();
            for (String starPositionKey : starPositionKeys) {
                Utils.isKeyValid(item, starPositionKey, CameraCoordinate.class);
                CameraCoordinate starPosition = (CameraCoordinate) item.get(starPositionKey);

                showerPixel = removeStarIslands(showerPixel, starPosition, starSet, starRadiusInCamera, log);
                if (showDifferentCleaningSets == true) {
                    addLevelToDataItem(showerPixel, outputKey + "_level7", item);
                    item.put("Starset", starSet);
                }
            }
        }

        item.put(outputKey, showerPixel);

        return item;
    }

    /**
     * Remove pixels with less than minNumberOfNeighborPixel neighboring shower pixel,
     * which arrival time differs more than the timeThreshold from the current pixel
     *
     * @param showerPixel
     * @param arrivalTime
     * @param timeThreshold
     * @param minNumberOfNeighborPixel
     * @return
     */
    public PixelSet applyTimeNeighborCleaning(PixelSet showerPixel, double[] arrivalTime, double timeThreshold, int minNumberOfNeighborPixel) {

        PixelSet newShowerPixels = new PixelSet();

        for (CameraPixel pixel : showerPixel) {

            CameraPixel[] currentNeighbors = pixelMap.getNeighborsForPixel(pixel);
            int counter = 0;
            double time = arrivalTime[pixel.id];

            for (CameraPixel nPix : currentNeighbors) {
                if (Math.abs(arrivalTime[nPix.id] - time) < timeThreshold && showerPixel.contains(nPix)) {
                    counter++;
                }
            }
            if (counter >= minNumberOfNeighborPixel) {
                newShowerPixels.add(pixel);
            }
        }
        return newShowerPixels;
    }
}
