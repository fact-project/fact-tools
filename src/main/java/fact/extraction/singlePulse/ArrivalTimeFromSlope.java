/**
 *
 */
package fact.extraction.singlePulse;


import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Finds pulse arrival time by finding the maximum slope in the leading edges of pulses or by finding the beginning of the pulse.
 * Also can find values to use for a baseline for calculating individual pulse sizes
 *
 * @author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 */
public class ArrivalTimeFromSlope implements Processor {
    static Logger log = LoggerFactory.getLogger(ArrivalTimeFromSlope.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String derivationKey;
    //slopes at each time slice; used features.Derivation

    @Parameter(required = true)
    public String outputKey;
    //positions of arrival times

    @Parameter(required = false)
    public String visualizeKey;
    //array of size data.length with values of zero except for time slices of arrival times.

    @Parameter(required = false)
    public String baselineKey;
    //used in OpenShutterPulseSize to account for negative values

    private int skipFirstSlices = 0;
    //start searching after this number of slices
    private int skipLastSlices = 0;
    //stop searching this many slices before the end of the timeline
    private int width = 1;
    //should be an odd number.

    @Override
    public Data process(Data item) {
        if (width % 2 == 0) {
            width++;
            log.info("ArrivalTimeFromSlope only supports odd window lengths. New length is: " + width);
        }

        double[] data = (double[]) item.get(key);
        double[] slopes = (double[]) item.get(derivationKey);
        int roi = data.length / Constants.N_PIXELS;

        ArrayList<ArrayList<Integer>> pulsePeaks = new ArrayList<>(Constants.N_PIXELS);
        //the position where pulse leading edges end
        int[][] arrivalTimes = new int[Constants.N_PIXELS][];
        //arrival times for all pulses in each pixel
        double[][] baselineValues = new double[Constants.N_PIXELS][];
        //value at the slice where you want to set your baseline
        double[] visualizePositions = new double[data.length];
        //zero for all positions except where an arrival time is found

        for (int i = 0; i < data.length; i++) {
            visualizePositions[i] = 0;
        }

        //for each pixel
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            ArrayList<Integer> currentPulsePeaks = findPulsePeaks(pix, roi, slopes);
            pulsePeaks.add(currentPulsePeaks);

            arrivalTimes[pix] = new int[pulsePeaks.size()];
            baselineValues[pix] = new double[pulsePeaks.size()];


            arrivalTimes[pix] = findArrivalTimes(pix, roi, width, data, slopes, pulsePeaks, visualizePositions, baselineValues);
        }

        item.put(outputKey, arrivalTimes);
        item.put(visualizeKey, visualizePositions);
        item.put(baselineKey, baselineValues);
//        System.out.println(Arrays.toString(baselineValues));


        return item;
    }

    /**
     * @param pix - Pixel to check
     * @param roi - the number of slices in one event
     */

    //the function that finds the pulses. returns positions of the peaks
    public ArrayList<Integer> findPulsePeaks(int pix, int roi, double[] slopes) {

        ArrayList<Integer> peaks = new ArrayList<>();
        int risingEdgeLength = 10;


        for (int slice = skipFirstSlices; slice < roi - skipLastSlices; slice++) {
            int pos = pix * roi + slice;
            boolean peak = true;
            boolean check = false;
            //allows one slice to have a negative slope in the leading edge


            if (slopes[pos] <= 0) {
                continue;
            } else {
                for (int i = 0; i < risingEdgeLength; i++) {
                    if (slice - i >= 0 && slice + i < roi) {
                        if (slopes[pos - i] < 0) {
                            if (check) {
                                peak = false;
                                break;
                            } else {
                                check = true;
                            }
                        }
                    } else {
                        peak = false;
                        break;
                    }
                }
            }

            if (!peak) {
                continue;
            }

            int k = 0;
            while (slice + k < roi) {
                if (slopes[pos + k] > 0) {
                    k++;
                } else {
                    break;
                }
            }
            slice += k - 1;

            peaks.add(slice);
        }

        return peaks;
    }


    //the function that finds the starting point of the pulse, defined by the first position with a positive slope, and
//the position of maximum slope. both values can be used for arrival time or baseline values
    public int[] findArrivalTimes(int pix, int roi, int width, double[] data, double[] slopes, ArrayList<ArrayList<Integer>> pulsePeaks, double[] visualizePositions, double[][] baselineValues) {
        ArrayList<Integer> times = new ArrayList<>();
        ArrayList<Double> baseValues = new ArrayList<>();
        ArrayList<Integer> peaks = pulsePeaks.get(pix);

        int pivot = (int) (width / 2.0);

        for (Integer peak : peaks) {
            int end = peak;

            //find the starting point of the leading edge
            int current = end;
            while (slopes[pix * roi + current - 1] > 0) {
                current--;
            }
            int start = current;        //start is the first position of the leading edge

//			accounting for 'false positives':
            if (pix * roi + end < data.length && pix * roi + start > 0) {
                double difference = data[pix * roi + end] - data[pix * roi + start];
                if (difference < 7) {
                    continue;
                }
            }

            //find max slope over leading edge
            int maxpos = 0;
            double maxslope = 0;
            for (int slice = start; slice < end; slice++) {
                int pos = pix * roi + slice;

                if (width == 1) {
                    double currentslope = slopes[pos];
                    if (currentslope > maxslope) {
                        maxslope = currentslope;
                        maxpos = slice;
                    }
                } else {
                    if (slice + pivot < end && slice - pivot > start) {
                        double currentslope = data[pos + pivot] - data[pos - pivot];
                        if (currentslope > maxslope) {
                            maxslope = currentslope;
                            maxpos = slice;
                        }
                    }
                }
            }

            if (start > skipFirstSlices && end < roi - skipLastSlices && end - maxpos < 14) {
                visualizePositions[pix * roi + start] = 15;
                times.add(start);
                baseValues.add(data[pix * roi + start]);
            }
            //to use maximum slope instead of first position of rising edge, simply replace start with maxpos.
        }
        baselineValues[pix] = Utils.arrayListToDouble(baseValues);
        return Utils.arrayListToInt(times);
    }
}
