package fact.TriggerEmulation;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import static fact.TriggerEmulation.Discriminator.*;
import static fact.TriggerEmulation.EmulateLogic.hasTriggered;

/**
 * Scan the TriggerThreshold for each event in order to find the maximum possible threshold to keep the event
 *
 *  * Created by jbuss on 26.08.18.
 */
public class findMaximumTriggerThreshold implements Processor {
    static Logger log = LoggerFactory.getLogger(findMaximumTriggerThreshold.class);

    @Parameter(required = true, description = "2D array of summed patches [double]")
    public String key;

    @Parameter(required = true, description = "maximum possible threshold for event to pass trigger decission")
    public String outKey;

    @Parameter(required = false,
            description = "Converts the patch array into a 1440*ROI array inorder to visualize the patche time series in the viewer")
    public Boolean visualize = false;

    @Parameter(required = false,
            description = "threshold of the discriminator in DAC units")
    public int minThreshold = 0;

    @Parameter(required = false,
            description = "threshold of the discriminator in DAC units")
    public int maxThreshold = 10000;

    @Parameter(required = false,
            description = "threshold of the discriminator in DAC units")
    public int thresholdIncrement = 8;

    @Parameter(required = false,
            description = "minimum time the signal has to stay above the threhold")
    public int minTimeOverThreshold = 8;

    @Parameter(required = false,
            description = "number of slices to ignore at the beginning of the time series")
    public int skipFirst = 10;

    @Parameter(required = false,
            description = "number of slices to ignore at the end of the time series")
    public int skipLast = 40;

    @Parameter(required = false,
            description = "minimum number of trigger patches per trigger unit to have a signal above threshold")
    public int nOutOf4 = 1;

    @Parameter(required = false,
            description = "minimum number of trigger units to have a signal above threshold")
    public int nOutOf40 = 1;

    @Parameter(required = false,
            description = "size of the time window within which the rising edges of the triggerprimitives should be")
    public int timeWindowSize = 12;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[][].class);
        double[][] data = (double[][]) item.get(key);

        int n_patches = Constants.N_PIXELS/Constants.N_PIXELS_PER_PATCH;
        double millivoltPerDAC = Constants.MILLIVOLT_PER_DAC;


        Integer lastThreshold = null;
        boolean[] lastprimitives = null;
        int[] lastSlices = null;

        for (int threshold = minThreshold; threshold <= maxThreshold; threshold+=thresholdIncrement) {

            DiscriminatorOutput[] discriminatorOutputs = discriminatePatches(
                    data,
                    threshold,
                    minTimeOverThreshold,
                    skipFirst,
                    skipLast
            );

            int[] patchTriggerSlice = discriminatorOutputsToTriggerSliceArray(discriminatorOutputs);

            boolean[] triggerPrimitives = discriminatorOutputsToTriggerPrimitiveArray(discriminatorOutputs);


            boolean triggerDecision = hasTriggered(
                    triggerPrimitives,
                    patchTriggerSlice,
                    nOutOf4,
                    nOutOf40,
                    timeWindowSize);

            if (triggerDecision){
                lastThreshold = threshold;
                lastprimitives = triggerPrimitives;
                lastSlices = patchTriggerSlice;
            }
            else {
                break;
            }
        }
        log.debug("MaxPossibleThreshold "+lastThreshold+" DAC / "+ dacToMillivolt(lastThreshold)+" mV"  );
        item.put(outKey, lastThreshold);
        item.put(outKey+"_primitives", lastprimitives);
        item.put(outKey+"_slices", lastSlices);

        return item;
    }
}
