package fact.extraction;

import fact.Constants;
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
    public String photonChargeKey = null;

    @Parameter(required = true, description = "Key to the photonCharge Array, calculated by the time over threshold processor")
    public String photonChargeSaturatedKey = null;

    @Parameter(required = true, description = "Key to the arrivalTime Array, calculated by the normal processor")
    public String arrivalTimeKey = null;

    @Parameter(required = true, description = "Key to the arrivalTime Array, calculated by the time over threshold processor")
    public String arrivalTimeSaturatedKey = null;

    @Parameter(required = true, description = "Limit above the time over threshold photoncharge is used [phe]. A good value is around 180")
    public double limitForSaturatedPixel;

    @Parameter(required = true)
    public String outputKeyPhotonCharge = null;

    @Parameter(required = true)
    public String outputKeyArrivalTime = null;

    @Parameter(description = "Key for Pixel Set of saturated Pixels")
    public String saturatedPixelKey = null;

    private PixelSet saturatedPixelSet = null;

    public Data process(Data item) {

        Utils.isKeyValid(item, photonChargeKey, double[].class);
        Utils.isKeyValid(item, photonChargeSaturatedKey, double[].class);
        Utils.isKeyValid(item, arrivalTimeKey, double[].class);
        Utils.isKeyValid(item, arrivalTimeSaturatedKey, double[].class);

        double[] photonCharge = (double[]) item.get(photonChargeKey);
        double[] photonChargeSaturated = (double[]) item.get(photonChargeSaturatedKey);
        double[] arrivalTime = (double[]) item.get(arrivalTimeKey);
        double[] arrivalTimeSaturated = (double[]) item.get(arrivalTimeSaturatedKey);

        double[] resultPhotonCharge = new double[photonCharge.length];
        System.arraycopy(photonCharge, 0, resultPhotonCharge, 0, photonCharge.length);
        double[] resultArrivalTimes = new double[arrivalTime.length];
        System.arraycopy(arrivalTime, 0, resultArrivalTimes, 0, arrivalTime.length);

        saturatedPixelSet = new PixelSet();

        for (int px = 0; px < Constants.N_PIXELS; px++) {
            if (photonCharge[px] > limitForSaturatedPixel) {
                resultArrivalTimes[px] = arrivalTimeSaturated[px];
                resultPhotonCharge[px] = photonChargeSaturated[px];
                saturatedPixelSet.addById(px);
            }
        }

        item.put(outputKeyArrivalTime, resultArrivalTimes);
        item.put(outputKeyPhotonCharge, resultPhotonCharge);

        if (saturatedPixelSet.toIntArray().length != 0) {
            item.put(saturatedPixelKey, saturatedPixelSet.toIntArray());
        }
        item.put(saturatedPixelKey + "Overlay", saturatedPixelSet);

        return item;
    }
}
