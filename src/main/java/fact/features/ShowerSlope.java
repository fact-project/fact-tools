package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.CameraPixel;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class ShowerSlope implements Processor {

    @Parameter(required = true)
    public String photonChargeKey = null;

    @Parameter(required = true)
    public String arrivalTimeKey = null;

    @Parameter(required = true)
    public String pixelSetKey = null;

    @Parameter(required = true)
    public String cogKey = null;

    @Parameter(required = true)
    public String deltaKey = null;

    @Parameter(required = true)
    public String slopeLongOutputKey = null;

    @Parameter(required = true)
    public String slopeTransOutputKey = null;

    @Parameter(required = true)
    public String slopeSpreadOutputKey = null;

    @Parameter(required = true)
    public String slopeSpreadWeightedOutputKey = null;

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, photonChargeKey, arrivalTimeKey, pixelSetKey, cogKey, deltaKey);
        Utils.isKeyValid(item, pixelSetKey, PixelSet.class);
        Utils.isKeyValid(item, cogKey, CameraCoordinate.class);

        double[] photonCharge = (double[]) item.get(photonChargeKey);
        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);
        PixelSet shower = (PixelSet) item.get(pixelSetKey);
        CameraCoordinate cog = (CameraCoordinate) item.get(cogKey);
        double delta = (Double) item.get(deltaKey);

        // NumberShowerPixel
        int n = shower.size();
        // in shower coordinates rotated x coord of the shower pixel
        double x[] = new double[n];
        // in shower coordinates rotated x coord of the shower pixel
        double y[] = new double[n];
        // Times of shower pixel
        double t[] = new double[n];
        // Weights of shower pixel
        double w[] = new double[n];

        int counter = 0;
        for (CameraPixel pixel: shower) {
            int chid = pixel.id;
            double xcoord = pixel.getXPositionInMM();
            double ycoord = pixel.getYPositionInMM();
            double[] rotPixels = Utils.transformToEllipseCoordinates(xcoord, ycoord, cog.xMM, cog.yMM, delta);
            x[counter] = rotPixels[0];
            y[counter] = rotPixels[1];
            t[counter] = arrivalTime[chid];
            w[counter] = photonCharge[chid];
            counter++;
        }

        // Calculate several element wise multiplication
        double[] xt = Utils.arrayMultiplication(x, t);
        double[] yt = Utils.arrayMultiplication(y, t);
        double[] xx = Utils.arrayMultiplication(x, x);
        double[] yy = Utils.arrayMultiplication(y, y);

        // Calculate several sums of arrays
        double sumx = Utils.arraySum(x);
        double sumy = Utils.arraySum(y);
        double sumt = Utils.arraySum(t);
        double sumxt = Utils.arraySum(xt);
        double sumyt = Utils.arraySum(yt);
        double sumxx = Utils.arraySum(xx);
        double sumyy = Utils.arraySum(yy);

        double slopeLong = (n * sumxt - sumt * sumx) / (n * sumxx - sumx * sumx);
        double slopeTrans = (n * sumyt - sumt * sumy) / (n * sumyy - sumy * sumy);

        // Calculate the difference from 0 (in time) per Pixel for a linear
        // dependency, described by slopeLong
        double b[] = new double[n];
        for (int i = 0; i < n; i++) {
            b[i] = t[i] - slopeLong * x[i];
        }
        double[] bb = Utils.arrayMultiplication(b, b);

        double sumw = Utils.arraySum(w);
        double sumb = Utils.arraySum(b);
        double sumbb = Utils.arraySum(bb);
        double sumwb = Utils.arraySum(Utils.arrayMultiplication(w, b));
        double sumwbb = Utils.arraySum(Utils.arrayMultiplication(w, bb));

        double slopeSpread = Math.sqrt(sumbb / n - Math.pow(sumb / n, 2));
        double slopeSpreadWeighted = Math.sqrt(sumwbb / sumw - Math.pow(sumwb / sumw, 2));

        item.put(slopeLongOutputKey, slopeLong);
        item.put(slopeTransOutputKey, slopeTrans);
        item.put(slopeSpreadOutputKey, slopeSpread);
        item.put(slopeSpreadWeightedOutputKey, slopeSpreadWeighted);
        return item;
    }
}
