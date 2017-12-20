package fact.extraction;

import fact.Utils;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * TODO: This is a work in progress.
 *
 * @author Fabian Temme
 */
public class HandleSaturation implements Processor {
    @Parameter(required = true, description = "Key to the photonCharge Array, calculated by the normal processor")
    private String photonChargeKey = null;
    @Parameter(required = true, description = "Key to the photonCharge Array, calculated by the time over threshold processor")
    private String photonChargeSaturatedKey = null;
    @Parameter(required = true, description = "Key to the arrivalTime Array, calculated by the normal processor")
    private String arrivalTimeKey = null;
    @Parameter(required = true, description = "Key to the arrivalTime Array, calculated by the time over threshold processor")
    private String arrivalTimeSaturatedKey = null;
    @Parameter(required = true, description = "Limit above the time over threshold photoncharge is used [phe]. A good value is around 180")
    private double limitForSaturatedPixel;
    @Parameter(required = true)
    private String outputKeyPhotonCharge = null;
    @Parameter(required = true)
    private String outputKeyArrivalTime = null;
    @Parameter(description = "Key for Pixel Set of saturated Pixels")
    private String saturatedPixelKey = null;

    private PixelSet saturatedPixelSet = null;

    private int npix;

    public Data process(Data input) {

        Utils.isKeyValid(input, photonChargeKey, double[].class);
        Utils.isKeyValid(input, photonChargeSaturatedKey, double[].class);
        Utils.isKeyValid(input, arrivalTimeKey, double[].class);
        Utils.isKeyValid(input, arrivalTimeSaturatedKey, double[].class);
        Utils.isKeyValid(input, "NPIX", Integer.class);

        double[] photonCharge = (double[]) input.get(photonChargeKey);
        double[] photonChargeSaturated = (double[]) input.get(photonChargeSaturatedKey);
        double[] arrivalTime = (double[]) input.get(arrivalTimeKey);
        double[] arrivalTimeSaturated = (double[]) input.get(arrivalTimeSaturatedKey);
        npix = (Integer) input.get("NPIX");


        double[] resultPhotonCharge = new double[photonCharge.length];
        System.arraycopy(photonCharge, 0, resultPhotonCharge, 0, photonCharge.length);
        double[] resultArrivalTimes = new double[arrivalTime.length];
        System.arraycopy(arrivalTime, 0, resultArrivalTimes, 0, arrivalTime.length);

        saturatedPixelSet = new PixelSet();

        for (int px = 0; px < npix; px++) {
            if (photonCharge[px] > limitForSaturatedPixel) {
                resultArrivalTimes[px] = arrivalTimeSaturated[px];
                resultPhotonCharge[px] = photonChargeSaturated[px];
                saturatedPixelSet.addById(px);
            }
        }

        input.put(outputKeyArrivalTime, resultArrivalTimes);
        input.put(outputKeyPhotonCharge, resultPhotonCharge);

        if (saturatedPixelSet.toIntArray().length != 0) {
            input.put(saturatedPixelKey, saturatedPixelSet.toIntArray());
        }
        input.put(saturatedPixelKey + "Overlay", saturatedPixelSet);

        return input;
    }


    public String getPhotonChargeKey() {
        return photonChargeKey;
    }


    public void setPhotonChargeKey(String photonChargeKey) {
        this.photonChargeKey = photonChargeKey;
    }


    public String getPhotonChargeSaturatedKey() {
        return photonChargeSaturatedKey;
    }


    public void setPhotonChargeSaturatedKey(String photonChargeSaturatedKey) {
        this.photonChargeSaturatedKey = photonChargeSaturatedKey;
    }


    public String getArrivalTimeKey() {
        return arrivalTimeKey;
    }


    public void setArrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }


    public String getArrivalTimeSaturatedKey() {
        return arrivalTimeSaturatedKey;
    }


    public void setArrivalTimeSaturatedKey(String arrivalTimeSaturatedKey) {
        this.arrivalTimeSaturatedKey = arrivalTimeSaturatedKey;
    }


    public double getLimitForSaturatedPixel() {
        return limitForSaturatedPixel;
    }


    public void setLimitForSaturatedPixel(double limitForSaturatedPixel) {
        this.limitForSaturatedPixel = limitForSaturatedPixel;
    }


    public String getOutputKeyPhotonCharge() {
        return outputKeyPhotonCharge;
    }


    public void setOutputKeyPhotonCharge(String outputKeyPhotonCharge) {
        this.outputKeyPhotonCharge = outputKeyPhotonCharge;
    }


    public String getOutputKeyArrivalTime() {
        return outputKeyArrivalTime;
    }


    public void setOutputKeyArrivalTime(String outputKeyArrivalTime) {
        this.outputKeyArrivalTime = outputKeyArrivalTime;
    }

    public String getSaturatedPixelKey() {
        return saturatedPixelKey;
    }

    public void setSaturatedPixelKey(String saturatedPixelKey) {
        this.saturatedPixelKey = saturatedPixelKey;
    }
}
