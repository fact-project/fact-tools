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
import java.util.Objects;

import static fact.TriggerEmulation.Discriminator.discriminatePatches;
import static fact.TriggerEmulation.Discriminator.discriminatorOutputsToTriggerPrimitiveArray;
import static fact.TriggerEmulation.Discriminator.discriminatorOutputsToTriggerSliceArray;

/**
 * Emulates a ratescan on summed patch time series.
 * Created by jbuss on 16.11.17.
 */
public class Ratescan implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(Ratescan.class);

    @Parameter(required = true,
               description = "double array[n_patches][roi] with time series from " +
                    "summed trigger patches")
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
        int n_patches = Constants.N_PATCHES;

        int nTiggeredPatches = n_patches;

        RatescanResult ratescanResult = new RatescanResult();

        for (int i = 0; nTiggeredPatches > 0 && i < nThresholds; i++) {
            int threshold = minThreshold+i*stepSize;

            ratescanResult.thresholdsArray.add(threshold);

            Discriminator.DiscriminatorOutput[] discriminatorOutputs = discriminatePatches(
                    data,
                    threshold,
                    minTimeOverThreshold,
                    skipFirst,
                    skipLast
            );

            int[] patchTriggerSlice = discriminatorOutputsToTriggerSliceArray(discriminatorOutputs);

            boolean[] currentTriggerPrimitives = discriminatorOutputsToTriggerPrimitiveArray(discriminatorOutputs);


            nTiggeredPatches = countPrimitives(currentTriggerPrimitives);

            log.debug("Threshold: "+threshold+" nPrimitives: "+nTiggeredPatches);

            ratescanResult.nPrimitivesArray.add(nTiggeredPatches);
            ratescanResult.triggerPrimitivesArray.add(currentTriggerPrimitives);
            ratescanResult.patchTriggerSlicesArray.add(patchTriggerSlice);

        }
        return ratescanResult;
    }

    /**
     * Output container for the ratescan
     */
    public class RatescanResult {
        public final ArrayList<Integer> thresholdsArray = new ArrayList<>();
        public final ArrayList<Integer> nPrimitivesArray = new ArrayList<>();
        public final ArrayList<boolean[]> triggerPrimitivesArray = new ArrayList<>();
        public final ArrayList<int[]> patchTriggerSlicesArray = new ArrayList<>();

        public int[] getThresholds(){
            return thresholdsArray.stream().filter(Objects::nonNull).mapToInt(j -> j).toArray();
        }

        public int[] getNumberOfPrimitives(){
            return nPrimitivesArray.stream().filter(Objects::nonNull).mapToInt(j -> j).toArray();
        }

        public boolean[][] getPrimitives(){
            boolean[][] primitives = new boolean[1][1];
            return triggerPrimitivesArray.toArray(primitives);
        }

        public int[][] getTriggerSlices(){
            int[][] triggerSlices = new int[1][1];
            return patchTriggerSlicesArray.toArray(triggerSlices);
        }
    }

    /**
     * Count the number of patches with a signal
     * @param primitives
     * @return
     */
    public static int countPrimitives(boolean[] primitives){
        int counts = 0;
        for (boolean primitive: primitives) {
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
