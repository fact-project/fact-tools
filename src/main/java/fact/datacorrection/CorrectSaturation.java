package fact.datacorrection;

import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import static org.apache.commons.math3.util.FastMath.exp;
import static org.apache.commons.math3.util.FastMath.pow;

/**
 * Created by jbuss on 28.01.15.
 */
public class CorrectSaturation implements Processor {
    @Parameter(required=true, description="")
    private String dataKey = null;

    @Parameter(required=true)
    private String outputKey = null;

    @Parameter(required=true, description="")
    private String totKey = null;

    @Parameter(required=true, description="")
    private String firstSliceOverThresholdKey = null;

    @Parameter(required=true, description="")
    private double  threshold = 1800.0;

    @Parameter(required=false, description="")
    private double  saturationThreshold = 1900.0;

    @Parameter(required=true, description="")
    private String maxPosKey = null;
    @Parameter(required=true, description="")
    private String baselineKey = null;
    private Integer npix                = 1440;
    private Integer roi                 = 300;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.mapContainsKeys(input, dataKey, totKey, maxPosKey, baselineKey, firstSliceOverThresholdKey, "NPIX");

        npix        = (Integer) input.get("NPIX");
        roi         = (Integer) input.get("NROI");

        double[] firstSlOverThresh = (double[]) input.get(firstSliceOverThresholdKey);
        int[] timeOverThreshold 	    = (int[]) input.get(totKey);

        int[] maxPos = (int[]) input.get(maxPosKey);
        double[] baselines  = (double[]) input.get(baselineKey);

        //get array with time series
        double[] data       = (double[]) input.get(dataKey);
        double[] corrData   =  data.clone();

        //Marker
        IntervalMarker[] markWidth          = new IntervalMarker[npix];
        IntervalMarker[] markMaxPos         = new IntervalMarker[npix];
        IntervalMarker[] markDeltaT         = new IntervalMarker[npix];
        IntervalMarker[] markEstArrivalTime = new IntervalMarker[npix];

        // ------------------------------------------------------------------------------------------------------------

        for(int pix = 0 ; pix < npix; pix++){
            int     firstSlice              = roi*pix;
            int     firstSliceAboveThresh   = (int)firstSlOverThresh[pix];
            int     maxPosInPix             = pix * roi + maxPos[pix];
            double  maxAmplitude            = data[maxPosInPix];

            //check if maxAmplitude fullfill saturation criterion
            if (saturationThreshold > maxAmplitude){
                continue;
            }
            markMaxPos[pix] = new IntervalMarker(maxPos[pix], maxPos[pix] + 1);

            //get the width of the pulse in order to compile the correction
            int width       = timeOverThreshold[pix];
            markWidth[pix]  = new IntervalMarker(firstSlOverThresh[pix],firstSlOverThresh[pix] + width);

            //estimate amplitude of saturated pulse (this formular is from MTreatSaturation.cc in Mars_Trunk Revision: 18096)
            // using an 4.polynom of the function
            double estimatedAmplitude = (threshold - baselines[pix]);
            estimatedAmplitude /= (0.898417 - 0.0187633*width + 0.000163919*pow(width,2) - 6.87369e-7*pow(width,3) + 1.13264e-9*pow(width,4));

            //calculate time difference between first slice over Threshold and arrival time of pulse;
            double deltaT       = -1.41371-0.0525846*width + 93.2763/(width+13.196);
            markDeltaT[pix]     = new IntervalMarker(maxPos[pix], maxPos[pix]+deltaT);

            //estimate arrival time
            double estimatedArrivalTime = firstSlOverThresh[pix] - deltaT -1;
            markEstArrivalTime[pix]     = new IntervalMarker(estimatedArrivalTime, estimatedArrivalTime + 1);

            // Loop over saturated slices and correct amplitudes
            for (int sl = firstSliceAboveThresh; sl < firstSliceAboveThresh + width && sl < roi; sl++) {

                int slice = sl + firstSlice;

                //check that slice is not outside of the current pixel's range
                if (slice >= roi*(pix+1)){
                    String message = "slice " + slice + " is is exceeding pixel " + pix;
                    throw new RuntimeException(message);
                }

                double t_0          = sl - estimatedArrivalTime;
                Double amplitude    = estimatedAmplitude*(1-1/(1+exp(t_0/2.14)))*exp(-t_0/38.8)+baselines[pix];

                if (amplitude > data[slice]){
                    corrData[slice] = amplitude;
                }
            }

        }

        input.put(outputKey + "_WidthMarker", markWidth);
        input.put(outputKey + "_MaxPosMarker", markMaxPos);
        input.put(outputKey + "_DeltaTMarker", markDeltaT);
        input.put(outputKey + "_estAtMarker", markEstArrivalTime);
        input.put(outputKey, corrData);

        return input;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getTotKey() {
        return totKey;
    }

    public void setTotKey(String totKey) {
        this.totKey = totKey;
    }

    public String getFirstSliceOverThresholdKey() {
        return firstSliceOverThresholdKey;
    }

    public void setFirstSliceOverThresholdKey(String firstSliceOverThresholdKey) {
        this.firstSliceOverThresholdKey = firstSliceOverThresholdKey;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getMaxPosKey() {
        return maxPosKey;
    }

    public void setMaxPosKey(String maxPosKey) {
        this.maxPosKey = maxPosKey;
    }

    public double getSaturationThreshold() {
        return saturationThreshold;
    }

    public void setSaturationThreshold(double saturationThreshold) {
        this.saturationThreshold = saturationThreshold;
    }

    public String getBaselineKey() {
        return baselineKey;
    }

    public void setBaselineKey(String baselineKey) {
        this.baselineKey = baselineKey;
    }
}
