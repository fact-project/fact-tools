package fact.extraction;

import fact.Utils;
import fact.container.PixelSet;
import fact.utils.RemappingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Identify pixel with a signal above a given threshold by means of photon chrage, hand them over as list and pixel array
 *
 * @author jbuss
 */
public class IdentifyPixelAboveThreshold implements Processor {
    static Logger log = LoggerFactory.getLogger(RemappingKeys.class);

    @Parameter(required = true, description = "The key to your data array.")
    private String key;
    @Parameter(required = true, description = "The threshold you want to check for.")
    private Integer threshold = 0;
    @Parameter(required = false)
    private String outputKey;

    private PixelSet pixelSet;

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        double[] matchArray = new double[npix];

        double[] featureArray = (double[]) input.get(key);

        pixelSet = new PixelSet();
        for (int pix = 0; pix < npix; pix++) {
            matchArray[pix] = 0;
            if (featureArray[pix] > threshold) {
                matchArray[pix] = 1;
                pixelSet.addById(pix);
            }
        }


        input.put(outputKey + "Set", pixelSet);

        input.put(outputKey, matchArray);

        // TODO Auto-generated method stub
        return input;
    }


    public String getKey() {
        return key;
    }


    public void setKey(String key) {
        this.key = key;
    }


    public Integer getThreshold() {
        return threshold;
    }


    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }


    public String getOutputKey() {
        return outputKey;
    }


    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }


    public PixelSet getPixelSet() {
        return pixelSet;
    }


    public void setPixelSet(PixelSet pixelSet) {
        this.pixelSet = pixelSet;
    }

}
