package fact.features;

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


public class ConcentrationCore implements Processor {
    static Logger log = LoggerFactory.getLogger(ConcentrationCore.class);

    @Parameter(required = true)
    public String outputKey;

    @Parameter(required = true, description = "Key of the Center of Gravity")
    public String cogKey;

    @Parameter(required = true, description = "Key of the delta angle")
    public String deltaKey;

    @Parameter(required = true, description = "Key of the sizeKey")
    public String sizeKey;

    @Parameter(required = true, description = "Key of the photoncharge array")
    public String photonChargeKey;

    @Parameter(required = true, description = "Key of the shower pixel array")
    public String pixelSetKey;

    @Parameter(required = true, description = "Key of the shower width")
    public String widthKey;

    @Parameter(required = true, description = "Key of the shower lengthKey")
    public String lengthKey;


    /**
     * Calculate the percentage of photons inside the Hillas Ellipse
     * aka. the pixels with a Mahalanobis Distance <= 1.
     */
    public Data process(Data item) {

        Utils.mapContainsKeys(item, cogKey, deltaKey, photonChargeKey, pixelSetKey, lengthKey, widthKey, sizeKey);
        Utils.isKeyValid(item, pixelSetKey, PixelSet.class);
        Utils.isKeyValid(item, cogKey, CameraCoordinate.class);

        CameraCoordinate cog = (CameraCoordinate) item.get(cogKey);
        Double delta = (Double) item.get(deltaKey);
        double[] photonChargeArray = (double[]) item.get(photonChargeKey);
        PixelSet showerPixelSet = (PixelSet) item.get(pixelSetKey);
        Double length = (Double) item.get(lengthKey);
        Double width = (Double) item.get(widthKey);
        Double size = (Double) item.get(sizeKey);


        double photonsInEllipse = 0;
        for (CameraPixel pix : showerPixelSet.set) {
            double px = pix.getXPositionInMM();
            double py = pix.getYPositionInMM();

            double[] ellipseCoords = Utils.transformToEllipseCoordinates(px, py, cog.xMM, cog.yMM, delta);

            // add a tolerance of 10% of the pixel size to not only get pixels with the center in the ellipse
            double dl = Math.abs(ellipseCoords[0]) - 0.1 * Constants.PIXEL_SIZE_MM;
            double dt = Math.abs(ellipseCoords[1]) - 0.1 * Constants.PIXEL_SIZE_MM;

            double distance = Math.pow(dl / length, 2.0) + Math.pow(dt / width, 2.0);

            if (distance <= 1) {
                photonsInEllipse += photonChargeArray[pix.id];
            }
        }
        double concCore = photonsInEllipse / size;
        item.put(outputKey, concCore);
        return item;
    }
}
