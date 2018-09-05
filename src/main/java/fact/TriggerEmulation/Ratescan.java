package fact.TriggerEmulation;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static fact.TriggerEmulation.Discriminator.booleanToInt;
import static fact.TriggerEmulation.Discriminator.discriminatePatches;

/**
 * Emulates a ratescan on summed patch time series
 * Created by jbuss on 16.11.17.
 */
public class Ratescan implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(Ratescan.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = false,
            description = "Int array [number of threshold] how many patches were triggered " +
                    "at given theshold")
    public String triggerCountsKey = "RatescanTriggerCounts";

    @Parameter(required = false,
            description = "int array [number of patches][number of threshold] " +
                    "containing each the first slice above threshold ")
    public String triggerSlicesKey = "RatescanTriggerSlices";

    @Parameter(required = false,
            description = "boolean array [number of patches][number of threshold] " +
                    "containing each if patch was triggered for given threshold ")
    public String triggerPrimitivesKey = "RatescanTriggerPrimitives";

    @Parameter(required = false,
            description = "int array [number of threshold] containing steps of thresholds ")
    public String thresholdsKey = "RatescanTriggerThresholds";

    @Parameter(required = false)
    public Integer minThreshold=0;

    @Parameter(required = false)
    public Integer nThresholds=null;

    @Parameter(required = false)
    public Integer stepSize=10;

    @Parameter(required = false,
            description = "minimum time the signal has to stay above the threshold")
    public int minTimeOverThreshold = 8;

    @Parameter(required = false,
            description = "number of slices to ignore at the beginning of the time series")
    public int skipFirst = 30;

    @Parameter(required = false,
            description = "number of slices to ignore at the end of the time series")
    public int skipLast = 40;



    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, key, double[][].class);
        double[][] data = (double[][]) item.get(key);

        RatescanResult ratescanResult = null;

        if (nThresholds == null){
            ratescanResult = ratescan( data, minThreshold, stepSize);
        } else {
            ratescanResult = ratescan( data, minThreshold, stepSize, nThresholds);
        }

        int[][] triggerSlices = ratescanResult.getTriggerSlices();
        int[] triggerCounts = ratescanResult.getNumberOfPrimitives();
        boolean[][] primitives = ratescanResult.getPrimitives();
        int[] thresholds = ratescanResult.getThresholds();

        item.put(triggerCountsKey, triggerCounts);
        item.put(triggerSlicesKey, triggerSlices);
        item.put(triggerPrimitivesKey, primitives);
        item.put(thresholdsKey, thresholds);

        return item;
    }

    public RatescanResult ratescan(
            double[][] data,
            int minThreshold,
            int stepSize
    ) {
        return ratescan( data, minThreshold, stepSize, Integer.MAX_VALUE);
    }

    /**
     * Performs a pseudo ratescan on the given data
     * @param data
     * @param minThreshold
     * @param stepSize
     * @param nThresholds
     */
    public RatescanResult ratescan(
            double[][] data,
            int minThreshold,
            int stepSize,
            int nThresholds
    ) {
        int n_patches = Constants.N_PIXELS/Constants.N_PIXELS_PER_PATCH;

        int n_tiggered_patches = n_patches;

        RatescanResult ratescanResult = new RatescanResult();

        for (int i = 0; n_tiggered_patches > 0 && i < nThresholds; i++) {
            int threshold = minThreshold+i*stepSize;

            ratescanResult.thresholds_arr.add(threshold);

            int[] patchTriggerSlice = new int[n_patches];

            boolean[] currentTriggerPrimitives = discriminatePatches(
                    data,
                    n_patches,
                    patchTriggerSlice,
                    threshold,
                    minTimeOverThreshold,
                    skipFirst,
                    skipLast
            );

            n_tiggered_patches = countPrimitives(currentTriggerPrimitives);

            log.debug("Threshold: "+threshold+" nPrimitives: "+n_tiggered_patches);

            ratescanResult.n_primitives_arr.add(n_tiggered_patches);
            ratescanResult.triggerPrimitives_arr.add(currentTriggerPrimitives);
            ratescanResult.patchTriggerSlices_arr.add(patchTriggerSlice);

        }
        return ratescanResult;
    }

    public static long lin2log(int z) {
        int x = 1;
        int y = 256;
        double b = Math.log(y/x)/(y-x);
        double a = 10 / Math.exp(b*10);
        double tempAnswer = a * Math.exp(b*z);
        long finalAnswer = Math.max(Math.round(tempAnswer) - 1, 0);

        return finalAnswer;

    }

    public class RatescanResult {
        ArrayList<Integer> thresholds_arr = new ArrayList<>();
        ArrayList<Integer> n_primitives_arr = new ArrayList<>();
        ArrayList<boolean[]> triggerPrimitives_arr = new ArrayList<>();
        ArrayList<int[]> patchTriggerSlices_arr = new ArrayList<>();

        public int[] getThresholds(){
            return thresholds_arr.stream().filter(Objects::nonNull).mapToInt(j -> j).toArray();
        }

        public int[] getNumberOfPrimitives(){
            return n_primitives_arr.stream().filter(Objects::nonNull).mapToInt(j -> j).toArray();
        }

        public boolean[][] getPrimitives(){
            boolean[][] primitives = new boolean[1][1];
            return triggerPrimitives_arr.toArray(primitives);
        }

        public int[][] getTriggerSlices(){
            int[][] triggerSlices = new int[1][1];
            return patchTriggerSlices_arr.toArray(triggerSlices);
        }
    }

    public int countPrimitives(boolean[] primitives){
        int counts = 0;
        for (boolean primitive:
             primitives) {
            if (primitive){
                counts++;
            }
        }
        return counts;
    }

    @Override
    public void init(ProcessContext context) throws Exception {

    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
