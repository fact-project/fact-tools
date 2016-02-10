package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Calculate the pixel sets that are illuminated by the given stars in the FOV
 *
 * Created by jbuss on 10.02.16.
 */
public class calculateStarSet implements Processor{
    static Logger log = LoggerFactory.getLogger(SimpleThreshold.class);

    @Parameter(required = true, defaultValue = "Cetatauri")
    private String[] starPositionKeys = null;
    @Parameter(required = false, defaultValue="Constants.PIXEL_SIZE")
    private double starRadiusInCamera = Constants.PIXEL_SIZE;
    @Parameter(required = false, defaultValue = "starset")
    private String outputKey="starset";

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {
        for (String starPositionKey : starPositionKeys) {
            Utils.isKeyValid(item, starPositionKey, double[].class);

            double[] starPosition = (double[]) item.get(starPositionKey);

            PixelSet starSet = calculateStarSet(starPosition, starRadiusInCamera, log);
            item.put(outputKey+":"+starPositionKey, starSet);
        }

        return item;
    }

    public PixelSet calculateStarSet(double[] starPosition, double starRadiusInCamera, Logger log) {

        PixelSet starSet = new PixelSet();

        FactCameraPixel pixel =  pixelMap.getPixelBelowCoordinatesInMM(starPosition[0], starPosition[1]);
        if (pixel == null){
            log.debug("Star not in camera window. No star islands are removed");
            return null;
        }
        starSet.add(pixel);

        for (FactCameraPixel px: pixelMap.getNeighboursForPixel(pixel))
        {
            if (calculateDistance(px, starPosition[0], starPosition[1]) < starRadiusInCamera)
            {
                starSet.add(px);
            }
        }
        return starSet;
    }

    private double calculateDistance(FactCameraPixel pixel, double x, double y) {

        double xdist = pixel.getXPositionInMM() - x;
        double ydist = pixel.getYPositionInMM() - y;

        return Math.sqrt((xdist*xdist)+(ydist*ydist));
    }

    public void setStarPositionKeys(String[] starPositionKeys) {
        this.starPositionKeys = starPositionKeys;
    }

    public void setStarRadiusInCamera(double starRadiusInCamera) {
        this.starRadiusInCamera = starRadiusInCamera;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
