package fact.TriggerEmulation;

import fact.Constants;
import fact.filter.ShapeSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import static fact.TriggerEmulation.Discriminator.booleanToInt;
import static fact.TriggerEmulation.Discriminator.discriminatePatch;
import static fact.TriggerEmulation.Discriminator.discriminatePatches;

/**
 * Emulate a discriminator that is working on the summed timeseries of the patches. Signals are digitized according to
 * the provided threshold and the minimum time a signal has to stay above it.
 * Created by jbuss on 15.11.17.
 */
public class EmulateDiscriminator implements Processor{

    static Logger log = LoggerFactory.getLogger(EmulateDiscriminator.class);

    @Parameter(required = true)
    private String key;

    @Parameter(required = false,
            description = "boolean array [number of patches] flagging if patch triggered ")
    private String primitivesKey = "TriggerPrimitives";

    @Parameter(required = false,
            description = "int array [number of patches] containing each the first slice above threshold ")
    private String triggerSliceKey = "TriggerSlice";

    @Parameter(required = false,
            description = "Converts the patch array into a 1440*ROI array inorder to visualize the patche time series in the viewer")
    private Boolean visualize = false;

    @Parameter(required = false,
            description = "threshold of the discriminator in DAC units")
    private int threshold = 330;

    @Parameter(required = false,
            description = "minimum time the signal has to stay above the threhold")
    private int minTimeOverThreshold = 8;

    @Parameter(required = false,
            description = "number of slices to ignore at the beginning of the time series")
    private int skipFirst = 10;

    @Parameter(required = false,
            description = "number of slices to ignore at the end of the time series")
    private int skipLast = 40;


    private int default_slice = Integer.MAX_VALUE;

    @Override
    public Data process(Data item) {
        double[][] data = (double[][]) item.get(key);

        int n_patches = Constants.NUMBEROFPIXEL/9;
        double millivoltPerDAC = Constants.MILLIVOLT_PER_DAC;


        int[] patchTriggerSlice = new int[n_patches];

        boolean[] triggerPrimitives = discriminatePatches(
                data,
                n_patches,
                millivoltPerDAC,
                patchTriggerSlice,
                threshold,
                minTimeOverThreshold,
                skipFirst,
                skipLast
        );

        if (visualize){
            putAsDataArray(item, n_patches, triggerPrimitives, patchTriggerSlice);
        }

        item.put(primitivesKey, triggerPrimitives);
        item.put(triggerSliceKey, patchTriggerSlice);

        return item;
    }

    /**
     * Convert triggerPrimitives and triggerSlice to arrays of length 1440 in order to visualize this in the viewer.
     * Put the arrays
     * @param item
     * @param n_patches
     * @param triggerPrimitives
     * @param patchTriggerSlice
     */
    private void putAsDataArray(Data item, int n_patches, boolean[] triggerPrimitives, int[] patchTriggerSlice) {
        int[] primitives = new int[1440];
        double[] triggerSlices = new double[1440];

        for (int patch = 0; patch < n_patches; patch++) {
            int primitive = booleanToInt(triggerPrimitives[patch]);
            for (int pix = 0; pix < 9 & primitive == 1; pix++) {
                primitives[patch*9+pix] = primitive;
                triggerSlices[patch*9+pix] = patchTriggerSlice[patch];
            }
        }
        item.put(primitivesKey+"_vis", primitives);
        item.put(triggerSliceKey+"_vis", triggerSlices);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPrimitivesKey(String primitivesKey) {
        this.primitivesKey = primitivesKey;
    }

    public void setTriggerSliceKey(String triggerSliceKey) {
        this.triggerSliceKey = triggerSliceKey;
    }

    public void setVisualize(Boolean visualize) {
        this.visualize = visualize;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }


    public void setMinTimeOverThreshold(int minTimeOverThreshold) {
        this.minTimeOverThreshold = minTimeOverThreshold;
    }

    public void setSkipFirst(int skipFirst) {
        this.skipFirst = skipFirst;
    }

    public void setSkipLast(int skipLast) {
        this.skipLast = skipLast;
    }
}
