package fact.features.source;

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


/**
 * generate a pixel set from the coordinates of stars in the camera given by the starPositionsKey
 * Created by jbuss on 30.08.18.
 */
public class PixelSetForStarPosition implements Processor {

    static Logger log = LoggerFactory.getLogger(PixelSetForStarPosition.class);

    @Parameter(required = true)
    public String starPositionsKey = null;

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
     * generates the pixel set with star pixels to be put to the data item
     * @param item
     * @return pixelset
     */
    public PixelSet calculateStarPixelSet(Data item) {
        PixelSet starSet = new PixelSet();

        if (starPositionsKey == null) {
            return starSet;
        }

        Utils.isKeyValid(item, starPositionsKey, CameraCoordinate[].class);
        CameraCoordinate[] starsInFOV = (CameraCoordinate[]) item.get(starPositionsKey);

        for (CameraCoordinate starInFOV : starsInFOV)
        {
            CameraPixel starPixel = pixelMap.getPixelBelowCoordinatesInMM(starInFOV.xMM, starInFOV.yMM);

            if (starPixel == null) {
                log.debug("Star not in camera window");
                continue;
            }
            starSet.add(starPixel);

            for (CameraPixel px : pixelMap.getNeighborsForPixel(starPixel)) {
                if (starInFOV.euclideanDistance(px.coordinate) < starRadiusInCamera) {
                    starSet.add(px);
                }
            }
        }
        return starSet;
    }
}
