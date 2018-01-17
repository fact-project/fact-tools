package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.utils.RemappingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Identify pixel with a signal above a given threshold by means of photon charge, hand them over as list and pixel array
 *
 * @author jbuss
 */
public class IdentifyPixelAboveThreshold implements Processor {
    static Logger log = LoggerFactory.getLogger(RemappingKeys.class);

    @Parameter(required = true, description = "The key to your data array.")
    public String key;

    @Parameter(required = true, description = "The threshold you want to check for.")
    public Integer threshold = 0;

    @Parameter(required = false)
    public String outputKey;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);

        double[] matchArray = new double[Constants.N_PIXELS];
        double[] featureArray = (double[]) input.get(key);

        PixelSet pixelSet = new PixelSet();
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            matchArray[pix] = 0;
            if (featureArray[pix] > threshold) {
                matchArray[pix] = 1;
                pixelSet.addById(pix);
            }
        }

        input.put(outputKey + "Set", pixelSet);
        input.put(outputKey, matchArray);

        return input;
    }
}
