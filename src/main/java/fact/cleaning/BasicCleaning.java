package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.calibrationservice.CalibrationService;
import fact.container.PixelSet;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import stream.Data;
import stream.annotations.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;


public class BasicCleaning {

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Service(required = true)
    public CalibrationService calibService;

    protected PixelSet notUsablePixelSet = null;


    /**
     * Add all pixel with a weight > corePixelThreshold to the showerpixel list.
     *
     * @param showerPixel
     * @param photonCharge
     * @param corePixelThreshold
     * @return
     */
    public PixelSet addCorePixel(PixelSet showerPixel, double[] photonCharge, double corePixelThreshold, ZonedDateTime eventTimeStamp) {
        PixelSet notUsablePixels = calibService.getNotUsablePixels(eventTimeStamp);

        for (int chid = 0; chid < Constants.N_PIXELS; chid++) {
            CameraPixel pixel = pixelMap.getPixelFromId(chid);
            if (notUsablePixels.contains(pixel)) {
                continue;
            }
            if (photonCharge[chid] > corePixelThreshold) {
                showerPixel.add(pixel);
            }
        }
        return showerPixel;
    }


    /**
     * add all neighboring pixels of the core pixels, with a weight > neighborPixelThreshold to the showerpixellist
     *
     * @param showerPixel
     * @param photonCharge
     * @return
     */
    public PixelSet addNeighboringPixels(PixelSet showerPixel, double[] photonCharge, double neighborPixelThreshold, ZonedDateTime eventTimeStamp) {
        PixelSet notUsablePixel = calibService.getNotUsablePixels(eventTimeStamp);
        PixelSet newShowerPixel = new PixelSet();
        newShowerPixel.addAll(showerPixel);
        for (CameraPixel pixel : showerPixel) {
            CameraPixel[] neighborPixels = pixelMap.getNeighborsFromID(pixel.id);

            for (CameraPixel neighborPixel : neighborPixels) {
                if (notUsablePixel.contains(neighborPixel)) {
                    continue;
                }
                if (photonCharge[neighborPixel.id] > neighborPixelThreshold) {
                    newShowerPixel.add(neighborPixel);
                }
            }
        }
        return newShowerPixel;
    }


    /**
     * Remove all clusters of pixels with less than minNumberOfPixel pixels in the cluster
     *
     * @param showerPixel
     * @param minNumberOfPixel
     * @return
     */
    public PixelSet removeSmallCluster(PixelSet showerPixel, int minNumberOfPixel) {
        ArrayList<PixelSet> clusters = Utils.breadthFirstSearch(showerPixel);
        PixelSet newShowerPixel = new PixelSet();

        for (PixelSet cluster : clusters) {
            if (cluster.size() >= minNumberOfPixel) {
                newShowerPixel.addAll(cluster);
            }
        }
        return newShowerPixel;
    }

    /**
     * Remove pixel clusters which contains only pixels around a star
     *
     * @param showerPixel
     * @param starPosition
     * @param starSet            PixelOverlay which contains the pixels around the star
     * @param starRadiusInCamera Radius around the star position, which defines, which pixels are declared as star pixel
     * @param log
     * @return
     */
    public PixelSet removeStarIslands(PixelSet showerPixel, CameraCoordinate starPosition, PixelSet starSet, double starRadiusInCamera, Logger log) {

        CameraPixel starPixel = pixelMap.getPixelBelowCoordinatesInMM(starPosition.xMM, starPosition.yMM);
        if (starPixel == null) {
            log.debug("Star not in camera window. No star islands are removed");
            PixelSet pixelSet = new PixelSet();
            pixelSet.addAll(showerPixel);
            return pixelSet;
        }

        starSet.add(starPixel);

        for (CameraPixel px : pixelMap.getNeighborsForPixel(starPixel)) {
            if (calculateDistance(px.id, starPosition.xMM, starPosition.yMM) < starRadiusInCamera) {
                starSet.add(px);
            }
        }

        ArrayList<PixelSet> clusters = Utils.breadthFirstSearch(showerPixel);

        PixelSet newShowerPixel = new PixelSet();
        for (PixelSet cluster : clusters) {
            if (!(cluster.size() <= starSet.size() && starSet.set.containsAll(cluster))) {
                newShowerPixel.addAll(cluster);
            }
        }
        return newShowerPixel;
    }

    public void addLevelToDataItem(PixelSet showerPixel, String name, Data item) {
        PixelSet overlay = new PixelSet();
        overlay.addAll(showerPixel);
        item.put(name, overlay);
    }

    /**
     * Calculates the Distance between a pixel and a given position
     *
     * @param chid
     * @param x
     * @param y
     * @return
     */
    private double calculateDistance(int chid, double x, double y) {
        double xdist = pixelMap.getPixelFromId(chid).getXPositionInMM() - x;
        double ydist = pixelMap.getPixelFromId(chid).getYPositionInMM() - y;

        return Math.sqrt((xdist * xdist) + (ydist * ydist));
    }

    public void setCalibService(CalibrationService calibService) {
        this.calibService = calibService;
    }
}
