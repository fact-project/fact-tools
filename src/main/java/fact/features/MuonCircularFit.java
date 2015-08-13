package fact.features;


import fact.Constants;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.overlays.EllipseOverlay;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.shell.Run;

/**
 * Created by maxnoe on 12.08.15.
 *
 * Calculates center and radius of a ring according to
 * "Optimum circular fit to weighted data in multidimensional space", B.B. Chaudhuri and P.Kundu
 * Pattern Recognition Letters 14 (1993)
 * http://www.sciencedirect.com/science/article/pii/016786559390126X
 *
 * Equations (11) and (12)
 */
public class MuonCircularFit implements StatefulProcessor {

    @Parameter(required = false, description = "Key to the extracted photoncharges", defaultValue = "photoncharge")
    private String photonchargeKey = "photoncharge";
    @Parameter(required = false, description = "Key to the cleaningPixel", defaultValue = "shower")
    private String cleaningPixelKey = "shower";
    @Parameter(required = false, description = "Base for the Outputkeys, outputs are radius, x, y", defaultValue = "circfit_")
    private String outputKey = "circfit_";

    private FactPixelMapping mapping = FactPixelMapping.getInstance();
    private int npix = Constants.NUMBEROFPIXEL;
    private double[] pixel_x = new double[npix];
    private double[] pixel_y = new double[npix];

    public Data process(Data data) {
        if (!data.containsKey(photonchargeKey)) {
            throw new RuntimeException("photonchargeKey " + photonchargeKey + "not found in data item");
        }
        if (!data.containsKey(cleaningPixelKey)){
            throw new RuntimeException("cleaningPixelKey " + cleaningPixelKey + "not found in data item");
        }

        double[] photoncharge = (double[]) data.get(photonchargeKey);
        int[] cleaningPixel = (int[]) data.get(cleaningPixelKey);
        System.out.println(cleaningPixel);

        double mean_x = 0,
                mean_y = 0,
                photoncharge_sum = 0;

        for (int i = 0; i < cleaningPixel.length; i++) {
            int chid = cleaningPixel[i];
            photoncharge_sum += photoncharge[chid];
            mean_x += photoncharge[chid] * pixel_x[chid];
            mean_y += photoncharge[chid] * pixel_y[chid];
        }

        mean_x /= photoncharge_sum;
        mean_y /= photoncharge_sum;


        double A1 = 0,
                A2 = 0,
                B1 = 0,
                B2 = 0,
                C1 = 0,
                C2 = 0;

        for (int i = 0; i < cleaningPixel.length; i++) {
            int chid = cleaningPixel[i];
            double x = pixel_x[chid];
            double y = pixel_y[chid];
            double m = photoncharge[chid];
            A1 += m * (x - mean_x) * x;
            A2 += m * (y - mean_y) * x;
            B1 += m * (x - mean_x) * y;
            B2 += m * (y - mean_y) * y;
            C1 += 0.5 * m * (x - mean_x) * (Math.pow(x, 2) + Math.pow(y, 2));
            C2 += 0.5 * m * (y - mean_y) * (Math.pow(x, 2) + Math.pow(y, 2));
        }

        double center_x = (B2 * C1 - B1 * C2)/ (A1 * B2 - A2 * B1);
        double center_y = (A2 * C1 - A1 * C2)/ (A2 * B1 - A1 * B2);

        double numerator = 0;
        for (int i = 0; i < cleaningPixel.length; i++) {
            int chid = cleaningPixel[i];
            numerator += photoncharge[chid] * (Math.pow(pixel_x[chid] - center_x, 2) + Math.pow(pixel_y[chid] - center_y, 2));
        }

        double radius = Math.sqrt(numerator / photoncharge_sum);

        data.put(outputKey + "radius", radius);
        data.put(outputKey + "x", center_x);
        data.put(outputKey + "y", center_y);
        data.put("@ellipseOverlay", new EllipseOverlay(center_x, center_y, radius, radius, 0));

        return data;
    }

    @Override
    public void finish(){
        return;
    }

    public void resetState(){
        return;
    }

    public void init(ProcessContext processContext) {
        for (int chid = 0; chid < npix; chid++) {
            FactCameraPixel pixel = mapping.getPixelFromId(chid);
            pixel_x[chid] = pixel.getXPositionInMM();
            pixel_y[chid] = pixel.getYPositionInMM();
        }
    }


    public void setPhotonchargeKey(String photonchargeKey) {
        this.photonchargeKey = photonchargeKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setCleaningPixelKey(String cleaningPixelKey) {
        this.cleaningPixelKey = cleaningPixelKey;
    }
}
