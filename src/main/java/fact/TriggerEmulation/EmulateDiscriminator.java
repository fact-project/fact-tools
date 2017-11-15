package fact.TriggerEmulation;

import com.google.common.primitives.Ints;
import fact.Constants;
import fact.filter.ShapeSignal;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Created by jbuss on 15.11.17.
 */
public class EmulateDiscriminator implements Processor{

    static Logger log = LoggerFactory.getLogger(ShapeSignal.class);

    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outKey;

    @Parameter(required = false, description = "Converts the patch array into a 1440*ROI array inorder to visualize the patche time series in the viewer")
    private Boolean visualize = false;

    @Parameter(required = false)
    private int threshold = 330;

    @Parameter(required = false)
    double millivoltPerDAC = 610.*Math.pow(10,-3);

    @Parameter(required = false)
    private int minTimeOverThreshold = 8;

    @Override
    public Data process(Data item) {
        double[][] data = (double[][]) item.get(key);

        int n_patches = Constants.NUMBEROFPIXEL/9;
        int default_slice = Integer.MAX_VALUE;

        double millivoltPerDAC = 610.*Math.pow(10,-3);
        double thresholdInMillivolt = millivoltPerDAC*threshold;

        boolean[] triggerPrimitives = new boolean[n_patches];
        int[] patchTriggerSlice = new int[n_patches];

        for (int patch = 0; patch < n_patches; patch++) {
            triggerPrimitives[patch] = false;
            int counter = 0;
            patchTriggerSlice[patch] = default_slice;

            for (int slice = 0; slice < data[patch].length; slice++) {
                double slice_amplitude = data[patch][slice];

                if (slice_amplitude >= thresholdInMillivolt){
                    if (counter == 0){
                        patchTriggerSlice[patch] = slice;
                    }
                    counter++;
                }
                else if (slice_amplitude < thresholdInMillivolt){
                    counter = 0;
                    patchTriggerSlice[patch] = default_slice;
                }
                if (counter >= minTimeOverThreshold){
                    triggerPrimitives[patch] = true;
                    break;
                }
            }

            if (triggerPrimitives[patch] == false){
                patchTriggerSlice[patch] = default_slice;
            }
        }

        int triggerSlice = Ints.min(patchTriggerSlice);

        if (visualize){
            int[] primitives = new int[1440];
            double[] triggerSlices = new double[1440];

            for (int patch = 0; patch < n_patches; patch++) {
                int primitive = booleanToInt(triggerPrimitives[patch]);
                for (int pix = 0; pix < 9 & primitive == 1; pix++) {
                    primitives[patch*9+pix] = primitive;
                    triggerSlices[patch*9+pix] = patchTriggerSlice[patch];
                }
            }
            item.put(outKey+"_vis", primitives);
            item.put("patchTriggerSlice"+"_vis", triggerSlices);
        }

        item.put(outKey, triggerPrimitives);
        item.put("patchTriggerSlice", patchTriggerSlice);

        return item;
    }

    private static int booleanToInt(boolean value) {
        // Convert true to 1 and false to 0.
        if (value){
            return 1;
        }
        return 0;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutKey(String outKey) {
        this.outKey = outKey;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setMillivoltPerDAC(double millivoltPerDAC) {
        this.millivoltPerDAC = millivoltPerDAC;
    }

    public void setMinTimeOverThreshold(int minTimeOverThreshold) {
        this.minTimeOverThreshold = minTimeOverThreshold;
    }
}
