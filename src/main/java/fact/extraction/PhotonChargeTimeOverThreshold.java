/**
 *
 */
package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This feature is supposed to calculate the photon charge of a SiPM pulse
 * from number of slices above a given Threshold (aka Pulse Width). So far this feature
 * is tuned to the pulse shape (defined by the used SiPMs and electronics)
 * as it is produced by FACT
 *
 * @author <a href="mailto:jens.buss@tu-dortmund.de">Jens Buss</a>
 */
public class PhotonChargeTimeOverThreshold implements Processor {
    static Logger log = LoggerFactory.getLogger(PhotonChargeTimeOverThreshold.class);

    @Parameter(required = true, description = "")
    public String timeOverThresholdKey = null;

    @Parameter(required = true, description = "")
    public String thresholdKey = null;

    @Parameter(required = true)
    public String outputKey = null;

    private double threshold = 0;

    public Data process(Data item) {
        Utils.isKeyValid(item, timeOverThresholdKey, int[].class);
        Utils.isKeyValid(item, thresholdKey, Double.class);

        double[] chargeFromThresholdArray = new double[Constants.N_PIXELS];

        int[] timeOverThresholdArray = (int[]) item.get(timeOverThresholdKey);
        threshold = (Double) item.get(thresholdKey);

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {

            chargeFromThresholdArray[pix] = 0.;

            // validate parameters
            if (timeOverThresholdArray[pix] <= 0) {
                continue;
            }

            // ATTENTION: the following are MAGIC NUMBERS to define a function
            // that computes the charge from a pulses width @ a threshold of 1500 mV
            // this is a dirty Hack to check if saturated pixels can be reconstructed
            double par[] = {
                    -1.83 * Math.pow(10, -6),
                    0.027,
                    0.0009,
                    3.54
            };

            double charge = par[2] * threshold + par[3];
            charge += (par[0] * threshold + par[1]) * timeOverThresholdArray[pix];
            charge = Math.exp(charge);

            chargeFromThresholdArray[pix] = charge + 40;
        }

        //add times over threshold
        item.put(outputKey, chargeFromThresholdArray);

        return item;
    }
}
