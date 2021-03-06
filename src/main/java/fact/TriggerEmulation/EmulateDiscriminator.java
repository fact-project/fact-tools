package fact.TriggerEmulation;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import static fact.TriggerEmulation.Discriminator.*;

/**
 * Emulate a discriminator that is working on the summed timeseries of the patches. Signals are digitized according to
 * the provided threshold and the minimum time a signal has to stay above it.
 *
 * According to P.Voglers PhD thesis (DOI: 10.3929/ETHZ-A-010568419) the time over threshold can be assumed
 * as 4ns.
 * Created by jbuss on 15.11.17.
 */
public class EmulateDiscriminator implements Processor{

    static Logger log = LoggerFactory.getLogger(EmulateDiscriminator.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = false,
            description = "boolean array [number of patches] flagging if patch triggered ")
    public String primitivesKey = "TriggerPrimitives";

    @Parameter(required = false,
            description = "int array [number of patches] containing each the first slice above threshold ")
    public String triggerSliceKey = "TriggerSlice";

    @Parameter(required = false,
            description = "Converts the patch array into a 1440*ROI array inorder to visualize the patche time series in the viewer")
    public Boolean visualize = false;

    @Parameter(required = false,
            description = "threshold of the discriminator in DAC units")
    public int threshold = 330;

    @Parameter(required = false,
            description = "key in dataitem with threshold of the discriminator in DAC units. !!!!Overwrites threshold")
    public String thresholdKey = null;

    @Parameter(required = false,
            description = "minimum time the signal has to stay above the threhold")
    public int minTimeOverThreshold = 8;

    @Parameter(required = false,
            description = "number of slices to ignore at the beginning of the time series")
    public int skipFirst = 10;

    @Parameter(required = false,
            description = "number of slices to ignore at the end of the time series")
    public int skipLast = 40;

    @Override
    public Data process(Data item) {
        double[][] data = (double[][]) item.get(key);

        int n_patches = Constants.N_PATCHES;

        if (thresholdKey != null){
            Utils.isKeyValid(item, thresholdKey, Integer.class);
            threshold = (Integer) item.get(thresholdKey);
        }




        DiscriminatorOutput[] discriminatorOutputs = discriminatePatches(
                data,
                threshold,
                minTimeOverThreshold,
                skipFirst,
                skipLast
        );

        int[] patchTriggerSlice = discriminatorOutputsToTriggerSliceArray(discriminatorOutputs);

        boolean[] triggerPrimitives = discriminatorOutputsToTriggerPrimitiveArray(discriminatorOutputs);



        if (visualize){
            putAsDataArray(item, n_patches, triggerPrimitives, patchTriggerSlice);
        }

        item.put(primitivesKey, triggerPrimitives);
        item.put(triggerSliceKey, patchTriggerSlice);

        return item;
    }

    /**
     * Convert triggerPrimitive and triggerSlice to arrays of length 1440 in order to visualize this in the viewer.
     * Put the arrays
     * @param item
     * @param n_patches
     * @param triggerPrimitives
     * @param patchTriggerSlice
     */
    public void putAsDataArray(Data item, int n_patches, boolean[] triggerPrimitives, int[] patchTriggerSlice) {
        int[] primitives = new int[1440];
        double[] triggerSlices = new double[1440];

        for (int patch = 0; patch < n_patches; patch++) {
            int primitive = booleanToInt(triggerPrimitives[patch]);
            for (int pix = 0; pix < Constants.N_PIXELS_PER_PATCH & primitive == 1; pix++) {
                int pixel_id = patch*Constants.N_PIXELS_PER_PATCH+pix;

                primitives[pixel_id] = primitive;
                triggerSlices[pixel_id] = patchTriggerSlice[patch];
            }
        }
        item.put(primitivesKey+"_vis", primitives);
        item.put(triggerSliceKey+"_vis", triggerSlices);
    }
}
