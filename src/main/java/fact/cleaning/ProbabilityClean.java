package fact.cleaning;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;

/**
 * Wolfgangs idea after previous cleaning make a new cleaning depending on the distance to showeraxis.
 * TODO: find thresholds and check code. unit test?
 *
 * @author Fabian Temme
 */
public class ProbabilityClean extends BasicCleaning implements Processor {

    private String photonChargeKey = null;

    private String outputKey = null;

    private String deltaKey = null;
    private String cogxKey = null;
    private String cogyKey = null;

    private double probabilityThreshold;

    private double distanceCoeff = 1.0;
    private double distanceExp = -1.0;

    private double[] photoncharge = null;
    private double delta;
    private double cogx;
    private double cogy;

    private int npix;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        Utils.mapContainsKeys(input, photonChargeKey, deltaKey);

        photoncharge = (double[]) input.get(photonChargeKey);

        delta = (Double) input.get(deltaKey);

        cogx = (Double) input.get(cogxKey);
        cogy = (Double) input.get(cogyKey);

        PixelSet showerSet = new PixelSet();

        for (int chid = 0; chid < npix; chid++) {
            CameraPixel pixel = pixelMap.getPixelFromId(chid);
            double xpos = pixel.getXPositionInMM();
            double ypos = pixel.getYPositionInMM();
            double dist = Utils.calculateDistancePointToShowerAxis(cogx, cogy, delta, xpos, ypos);
            double weight = photoncharge[chid] * Math.pow(distanceCoeff * dist, distanceExp);

            if (weight > probabilityThreshold) {
                showerSet.add(pixel);
            }

        }

        showerSet = removeSmallCluster(showerSet, 2);

        input.put(outputKey, showerSet);

        return input;
    }

    public String getPhotonChargeKey() {
        return photonChargeKey;
    }

    public void setPhotonChargeKey(String photonChargeKey) {
        this.photonChargeKey = photonChargeKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getDeltaKey() {
        return deltaKey;
    }

    public void setDeltaKey(String deltaKey) {
        this.deltaKey = deltaKey;
    }

    public String getCogxKey() {
        return cogxKey;
    }

    public void setCogxKey(String cogxKey) {
        this.cogxKey = cogxKey;
    }

    public String getCogyKey() {
        return cogyKey;
    }

    public void setCogyKey(String cogyKey) {
        this.cogyKey = cogyKey;
    }

    public double getProbabilityThreshold() {
        return probabilityThreshold;
    }

    public void setProbabilityThreshold(double probabilityThreshold) {
        this.probabilityThreshold = probabilityThreshold;
    }

    public double getDistanceCoeff() {
        return distanceCoeff;
    }

    public void setDistanceCoeff(double distanceCoeff) {
        this.distanceCoeff = distanceCoeff;
    }

    public double getDistanceExp() {
        return distanceExp;
    }

    public void setDistanceExp(double distanceExp) {
        this.distanceExp = distanceExp;
    }

}
