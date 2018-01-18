package fact.datacorrection;

import fact.Constants;
import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import static org.apache.commons.math3.util.FastMath.exp;
import static org.apache.commons.math3.util.FastMath.pow;

/**
 * This processor is supposed to correct the amplitudes of a saturated pulse by using the pulse template.
 * It needs the time over threshold (totKey) that was measured at an amplitude of 1800 mV
 * Currently the conversion from tot to amplitude works only for this threshold
 * <p>
 * Created by jbuss on 28.01.15.
 */
public class CorrectSaturation implements Processor {
    @Parameter(required = true, description = "Array containing the raw data")
    public String dataKey = null;

    @Parameter(required = true, description = "Target Array for the saturation corrected raw data")
    public String outputKey = null;

    @Parameter(required = true, description = "1440pix array containing the time-over-threshold for the pixel")
    public String totKey = null;

    @Parameter(required = true, description = "1440pix array containing the first slice time-over-threshold for the pixel")
    public String firstSliceOverThresholdKey = null;

    @Parameter(required = true, description = "threshold at which the time-over-threshold was measured")
    public double threshold = 1800.0;

    @Parameter(required = false, description = "threshold that defines a pixel showing saturation")
    public double saturationThreshold = 1900.0;

    @Parameter(required = true, description = "1440pix array containing the slice of maximum amplitude")
    public String maxPosKey = null;

    @Parameter(required = true, description = "1440pix array containing the estimated baseline amplitude")
    public String baselineKey = null;

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, dataKey, totKey, maxPosKey, baselineKey, firstSliceOverThresholdKey, "NPIX");

        int roi = (int) item.get("NROI");

        double[] firstSlOverThresh = (double[]) item.get(firstSliceOverThresholdKey);
        int[] timeOverThreshold = (int[]) item.get(totKey);

        int[] maxPos = (int[]) item.get(maxPosKey);
        double[] baselines = (double[]) item.get(baselineKey);

        //get array with time series
        double[] data = (double[]) item.get(dataKey);
        double[] corrData = data.clone();

        //Marker
        IntervalMarker[] markWidth = new IntervalMarker[Constants.N_PIXELS];
        IntervalMarker[] markMaxPos = new IntervalMarker[Constants.N_PIXELS];
        IntervalMarker[] markDeltaT = new IntervalMarker[Constants.N_PIXELS];
        IntervalMarker[] markEstArrivalTime = new IntervalMarker[Constants.N_PIXELS];

        // ------------------------------------------------------------------------------------------------------------

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            int firstSlice = roi * pix;
            int firstSliceAboveThresh = (int) firstSlOverThresh[pix];
            int maxPosInPix = pix * roi + maxPos[pix];
            double maxAmplitude = data[maxPosInPix];

            //check if maxAmplitude fullfill saturation criterion
            if (saturationThreshold > maxAmplitude) {
                continue;
            }
            markMaxPos[pix] = new IntervalMarker(maxPos[pix], maxPos[pix] + 1);

            //get the width of the pulse in order to compile the correction
            int width = timeOverThreshold[pix];
            markWidth[pix] = new IntervalMarker(firstSlOverThresh[pix], firstSlOverThresh[pix] + width);

            //estimate amplitude of saturated pulse (this formular is from MTreatSaturation.cc in Mars_Trunk Revision: 18096)
            // using an 4.polynom of the function
            double estimatedAmplitude = (threshold - baselines[pix]);
            estimatedAmplitude /= (0.898417 - 0.0187633 * width + 0.000163919 * pow(width, 2) - 6.87369e-7 * pow(width, 3) + 1.13264e-9 * pow(width, 4));

            //calculate time difference between first slice over Threshold and arrival time of pulse;
            double deltaT = -1.41371 - 0.0525846 * width + 93.2763 / (width + 13.196);
            markDeltaT[pix] = new IntervalMarker(maxPos[pix], maxPos[pix] + deltaT);

            //estimate arrival time
            double estimatedArrivalTime = firstSlOverThresh[pix] - deltaT - 1;
            markEstArrivalTime[pix] = new IntervalMarker(estimatedArrivalTime, estimatedArrivalTime + 1);

            // Loop over saturated slices and correct amplitudes
            for (int sl = firstSliceAboveThresh; sl < firstSliceAboveThresh + width && sl < roi; sl++) {

                int slice = sl + firstSlice;

                //check that slice is not outside of the current pixel's range
                if (slice >= roi * (pix + 1)) {
                    String message = "slice " + slice + " is is exceeding pixel " + pix;
                    throw new RuntimeException(message);
                }

                double t_0 = sl - estimatedArrivalTime;
                Double amplitude = estimatedAmplitude * (1 - 1 / (1 + exp(t_0 / 2.14))) * exp(-t_0 / 38.8) + baselines[pix];

                if (amplitude > data[slice]) {
                    corrData[slice] = amplitude;
                }
            }

        }

        item.put(outputKey + "_WidthMarker", markWidth);
        item.put(outputKey + "_MaxPosMarker", markMaxPos);
        item.put(outputKey + "_DeltaTMarker", markDeltaT);
        item.put(outputKey + "_estAtMarker", markEstArrivalTime);
        item.put(outputKey, corrData);

        return item;
    }
}
