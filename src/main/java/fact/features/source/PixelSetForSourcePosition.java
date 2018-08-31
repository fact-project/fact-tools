package fact.features.source;

import fact.Constants;
import fact.TriggerEmulation.SumUpPatches;
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


/**
 * generate a pixel set from the coordinates of stars in the camera given by the starPositionKeys
 * Created by jbuss on 30.08.18.
 */
public class PixelSetForSourcePosition implements Processor {

    static Logger log = LoggerFactory.getLogger(PixelSetForSourcePosition.class);

    @Parameter(required = true)
    public String starPositionKeys[] = null;

    @Parameter(required = false)
    public String outsetKey;

    @Parameter(required = false, defaultValue="Constants.PIXEL_SIZE")
    public double starRadiusInCamera = Constants.PIXEL_SIZE_MM;

    private FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {

        PixelSet pixelset = calculateStarPixelSet(item);

        item.put(outsetKey, pixelset);

        return item;
    }

    /**
     * generates the pixel set with star pixels to be excluded from the trigger
     * @param item
     * @return pixelset
     */
    public PixelSet calculateStarPixelSet(Data item) {
        PixelSet starSet = new PixelSet();

        if (starPositionKeys == null) {
            return starSet;
        }

        for (String starPositionKey : starPositionKeys)
        {
            if (!item.containsKey(starPositionKey)){
                continue;
            }

            Utils.isKeyValid(item, starPositionKey, CameraCoordinate.class);
            CameraCoordinate starPosition = (CameraCoordinate) item.get(starPositionKey);

            CameraPixel starPixel = pixelMap.getPixelBelowCoordinatesInMM(starPosition.xMM, starPosition.yMM);

            if (starPixel == null) {
                log.debug("Star not in camera window");
                continue;
            }
            starSet.add(starPixel);

            for (CameraPixel px : pixelMap.getNeighborsForPixel(starPixel)) {
                if (calculateDistance(px.id, starPosition.xMM, starPosition.yMM) < starRadiusInCamera) {
                    starSet.add(px);
                }
            }
        }
        return starSet;
    }

    /**
     * Calculates the Distance between a pixel and a given position
     * @param chid
     * @param x
     * @param y
     * @return
     */
    private double calculateDistance(int chid, double x, double y)
    {
        double xdist = pixelMap.getPixelFromId(chid).getXPositionInMM() - x;
        double ydist = pixelMap.getPixelFromId(chid).getYPositionInMM() - y;

        return Math.sqrt((xdist*xdist)+(ydist*ydist));
    }

}
