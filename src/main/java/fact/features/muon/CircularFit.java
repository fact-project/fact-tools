package fact.features.muon;


import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/**
 * Created by maxnoe on 12.08.15.
 * <p>
 * Calculates center and radius of a ring according to
 * "Optimum circular fit to weighted data in multidimensional space", B.B. Chaudhuri and P. Kundu
 * Pattern Recognition Letters 14 (1993)
 * http://www.sciencedirect.com/science/article/pii/016786559390126X
 * <p>
 * Equations (11) and (12)
 */
public class CircularFit implements StatefulProcessor {

    @Parameter(required = false, description = "Key to the extracted photoncharges", defaultValue = "photoncharge")
    public String photonchargeKey = "photoncharge";

    @Parameter(required = false, description = "PixelSet to perform the fit on", defaultValue = "shower")
    public String pixelSetKey = "shower";

    @Parameter(required = false, description = "Base for the Outputkeys, outputs are radius, x, y", defaultValue = "circfit_")
    public String outputKey = "circFit";

    private FactPixelMapping mapping = FactPixelMapping.getInstance();
    private int npix = Constants.N_PIXELS;
    private double[] pixelX = new double[npix];
    private double[] pixelY = new double[npix];

    public Data process(Data item) {
        Utils.mapContainsKeys(item, photonchargeKey, pixelSetKey);

        double[] photoncharge = (double[]) item.get(photonchargeKey);
        PixelSet pixelSet = (PixelSet) item.get(pixelSetKey);

        double mean_x = 0,
                mean_y = 0,
                photoncharge_sum = 0;

        for (CameraPixel pix : pixelSet.set) {
            photoncharge_sum += photoncharge[pix.id];
            mean_x += photoncharge[pix.id] * pixelX[pix.id];
            mean_y += photoncharge[pix.id] * pixelY[pix.id];
        }

        mean_x /= photoncharge_sum;
        mean_y /= photoncharge_sum;


        double A1 = 0,
                A2 = 0,
                B1 = 0,
                B2 = 0,
                C1 = 0,
                C2 = 0;

        for (CameraPixel pix : pixelSet.set) {
            double x = pixelX[pix.id];
            double y = pixelY[pix.id];
            double m = photoncharge[pix.id];
            A1 += m * (x - mean_x) * x;
            A2 += m * (y - mean_y) * x;
            B1 += m * (x - mean_x) * y;
            B2 += m * (y - mean_y) * y;
            C1 += 0.5 * m * (x - mean_x) * (Math.pow(x, 2) + Math.pow(y, 2));
            C2 += 0.5 * m * (y - mean_y) * (Math.pow(x, 2) + Math.pow(y, 2));
        }

        double center_x = (B2 * C1 - B1 * C2) / (A1 * B2 - A2 * B1);
        double center_y = (A2 * C1 - A1 * C2) / (A2 * B1 - A1 * B2);

        double numerator = 0;
        for (CameraPixel pix : pixelSet.set) {
            numerator += photoncharge[pix.id] * (Math.pow(pixelX[pix.id] - center_x, 2) + Math.pow(pixelY[pix.id] - center_y, 2));
        }

        double radius = Math.sqrt(numerator / photoncharge_sum);

        item.put(outputKey + "R", radius);
        item.put(outputKey + "X", center_x);
        item.put(outputKey + "Y", center_y);
        item.put(outputKey + "Circle", new EllipseOverlay(center_x, center_y, radius, radius, 0));

        return item;
    }

    @Override
    public void finish() {
        return;
    }

    public void resetState() {
        return;
    }

    public void init(ProcessContext processContext) {
        for (int chid = 0; chid < npix; chid++) {
            CameraPixel pixel = mapping.getPixelFromId(chid);
            pixelX[chid] = pixel.getXPositionInMM();
            pixelY[chid] = pixel.getYPositionInMM();
        }
    }
}
