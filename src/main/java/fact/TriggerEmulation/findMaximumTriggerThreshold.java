package fact.TriggerEmulation;

import fact.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import static fact.TriggerEmulation.Discriminator.discriminatePatches;
import static fact.TriggerEmulation.EmulateLogic.isTriggerDecision;

/**
 * Scan the TriggerThreshold for each event in order to find the maximum possible threshold to keep the event
 *
 *  * Created by jbuss on 26.08.18.
 */
public class findMaximumTriggerThreshold implements Processor {
    static Logger log = LoggerFactory.getLogger(findMaximumTriggerThreshold.class);

    @Parameter(required = true, description = "2D array of summed patches [doubl]")
    private String key;

    @Parameter(required = true, description = "maximum possible threshold for event to pass trigger decission")
    private String outKey;

    @Parameter(required = false,
            description = "Converts the patch array into a 1440*ROI array inorder to visualize the patche time series in the viewer")
    private Boolean visualize = false;

    @Parameter(required = false,
            description = "threshold of the discriminator in DAC units")
    private int minThreshold = 200;

    @Parameter(required = false,
            description = "threshold of the discriminator in DAC units")
    private int maxThreshold = 10000;

    @Parameter(required = false,
            description = "threshold of the discriminator in DAC units")
    private int thresholdIncrement = 4;

    @Parameter(required = false,
            description = "minimum time the signal has to stay above the threhold")
    private int minTimeOverThreshold = 8;

    @Parameter(required = false,
            description = "number of slices to ignore at the beginning of the time series")
    private int skipFirst = 10;

    @Parameter(required = false,
            description = "number of slices to ignore at the end of the time series")
    private int skipLast = 40;

    @Parameter(required = false,
            description = "minimum number of trigger patches per trigger unit to have a signal above threshold")
    private int nOutOf4 = 1;

    @Parameter(required = false,
            description = "minimum number of trigger units to have a signal above threshold")
    private int nOutOf40 = 1;

    @Parameter(required = false,
            description = "size of the time window within which the rising edges of the triggerprimitives should be")
    private int timeWindowSize = 12;

    @Override
    public Data process(Data item) {
        double[][] data = (double[][]) item.get(key);

        int n_patches = Constants.NUMBEROFPIXEL/9;
        double millivoltPerDAC = Constants.MILLIVOLT_PER_DAC;


        Integer lastThreshold = null;

        for (int threshold = minThreshold; threshold < maxThreshold; threshold++) {

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

            boolean triggerDecision = isTriggerDecision(
                    triggerPrimitives,
                    patchTriggerSlice,
                    nOutOf4,
                    nOutOf40,
                    timeWindowSize);

            if (triggerDecision){
                lastThreshold = threshold;
            }
            else {
                break;
            }
        }

        item.put(outKey, lastThreshold);

        return item;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutKey(String outKey) {
        this.outKey = outKey;
    }

    public void setVisualize(Boolean visualize) {
        this.visualize = visualize;
    }

    public void setMinThreshold(int minThreshold) {
        this.minThreshold = minThreshold;
    }

    public void setMaxThreshold(int maxThreshold) {
        this.maxThreshold = maxThreshold;
    }

    public void setThresholdIncrement(int thresholdIncrement) {
        this.thresholdIncrement = thresholdIncrement;
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

    public void setnOutOf4(int nOutOf4) {
        this.nOutOf4 = nOutOf4;
    }

    public void setnOutOf40(int nOutOf40) {
        this.nOutOf40 = nOutOf40;
    }

    public void setTimeWindowSize(int timeWindowSize) {
        this.timeWindowSize = timeWindowSize;
    }
}
