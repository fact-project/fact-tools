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
}
