package fact.TriggerEmulation;

import fact.Constants;
import fact.filter.ShapeSignal;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Emulates a ratescan on summed patch time series
 * Created by jbuss on 16.11.17.
 */
public class Ratescan implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(ShapeSignal.class);

    @Parameter(required = true)
    private String key;

    @Parameter(required = false,
            description = "Int array [number of patches][number of threshold] flaggin if patch was triggered at given theshold")
    private String triggerRatesKey = "TriggerRates";

    @Parameter(required = false,
            description = "int array [number of patches][number of threshold] containing each the first slice above threshold ")
    private String triggerSlicesKey = "TriggerSlices";

    @Parameter(required = false,
            description = "int array [number of threshold] containing steps threshold ")
    private String thresholdsKey = "TriggerThresholds";

    @Parameter(required = false)
    private Integer minThreshold=0;

    @Parameter(required = false)
    private Integer nThresholds=40;

    @Parameter(required = false)
    private Integer stepSize=20;

    @Parameter(required = false,
            description = "minimum time the signal has to stay above the threshold")
    private int minTimeOverThreshold = 8;

    @Parameter(required = false,
            description = "number of slices to ignore at the beginning of the time series")
    private int skipFirst = 10;

    @Parameter(required = false,
            description = "number of slices to ignore at the end of the time series")
    private int skipLast = 40;

    private int[] thresholds;


    @Override
    public Data process(Data item) {

        double[][] data = (double[][]) item.get(key);

        int n_patches = Constants.NUMBEROFPIXEL/9;

        int[][] patchTriggerSlice = new int[n_patches+1][nThresholds];
        int[][] patchTriggerRate = new int[n_patches+1][nThresholds];

        for (int patch = 0; patch < n_patches; patch++) {
            ratescan(data[patch], minThreshold, nThresholds, stepSize,
                    patchTriggerSlice[patch], patchTriggerRate[patch]);
            for (int i = 0; i < nThresholds; i++) {
                patchTriggerRate[n_patches][i] += patchTriggerRate[patch][i];
            }
        }

        item.put(triggerRatesKey, patchTriggerRate);
        item.put(triggerSlicesKey, patchTriggerSlice);
        item.put(thresholdsKey, thresholds);

        return item;
    }

    /**
     * Performs a pseudo ratescan on the given data
     * @param data
     * @param minThreshold
     * @param nThresholds
     * @param stepSize
     * @param triggerSlice
     * @param triggerRate
     */
    public void ratescan(
            double[] data,
            int minThreshold,
            int nThresholds,
            int stepSize,
            int[] triggerSlice,
            int[] triggerRate
    ) {
        for (int i = 0; i < nThresholds; i++) {

            int dac = minThreshold+i*stepSize;

            triggerSlice[i] =
                    EmulateDiscriminator.discriminatePatch(
                            data,
                            EmulateDiscriminator.thresholdDACToMillivolt(dac, Constants.MILLIVOLT_PER_DAC),
                            minTimeOverThreshold,
                            skipFirst,
                            skipLast
                    );

            if (triggerSlice[i] < Integer.MAX_VALUE) {
                triggerRate[i] = 1;
            }
        }
    }

    @Override
    public void init(ProcessContext context) throws Exception {
        int[] thresholds = new int[nThresholds];

        for (int i = 0; i < nThresholds; i++) {
            thresholds[i] = minThreshold+stepSize*i;
        }

        log.info("Performing ratesscan with thresholds: \n"+Arrays.toString(thresholds));
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
