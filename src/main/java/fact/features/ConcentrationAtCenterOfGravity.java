package fact.features;

import fact.Utils;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * @author Fabian Temme
 */
public class ConcentrationAtCenterOfGravity implements Processor {
    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    static Logger log = LoggerFactory.getLogger(ConcentrationAtCenterOfGravity.class);

    @Parameter(required = true, defaultValue = "photonCharge", description = "Key of the array of photoncharge.")
    public String photonChargeKey = null;

    @Parameter(required = true, description = "Key of the center of gravity of shower. (generate by HillasParameters)")
    public String cogKey = null;


    @Parameter(required = true, defaultValue = "Size", description = "Key of the size of the event. (Generated e.g. by Size processor.)")
    public String sizeKey = null;

    @Parameter(required = true, defaultValue = "concCOG", description = "The key of the generated value.")
    public String outputKey = null;

    private double[] photonCharge = null;


    /**
     * This function calculates the concentration at the center of gravity including the 2 nearest pixel
     */
    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, cogKey, sizeKey, photonChargeKey);
        Utils.isKeyValid(item, cogKey, CameraCoordinate.class);

        CameraCoordinate cog = (CameraCoordinate) item.get(cogKey);
        double size = (double) item.get(sizeKey);

        photonCharge = (double[]) item.get(photonChargeKey);
        CameraPixel cogPixel = pixelMap.getPixelBelowCoordinatesInMM(cog.xMM, cog.yMM);
        if (cogPixel == null) {
            item.put(outputKey, -Double.MAX_VALUE);
            return item;
        }
        CameraPixel[] neighbors = pixelMap.getNeighborsForPixel(cogPixel);

        // mindist1 < mindist2
        double mindist1 = Float.MAX_VALUE;
        double mindist2 = Float.MAX_VALUE;

        CameraPixel minChId1 = cogPixel;
        CameraPixel minChId2 = cogPixel;

        // search for the two nearest neighbors
        for (CameraPixel pix : neighbors) {
            double x = pix.getXPositionInMM();
            double y = pix.getYPositionInMM();
            double dist = (cog.xMM - x) * (cog.xMM - x) + (cog.yMM - y) * (cog.yMM - y);

            if (dist < mindist1) {
                mindist2 = mindist1;
                mindist1 = dist;
                minChId2 = minChId1;
                minChId1 = pix;
            } else if (dist < mindist2) {
                mindist2 = dist;
                minChId2 = pix;
            }
        }

        double conc = photonCharge[cogPixel.id] + photonCharge[minChId1.id] + photonCharge[minChId2.id];
        conc /= size;
        item.put(outputKey, conc);

        return item;
    }
}
