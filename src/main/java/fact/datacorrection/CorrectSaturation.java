package fact.datacorrection;

import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import static org.apache.commons.math3.util.FastMath.exp;

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
        int[] totArray 	    = (int[]) input.get(totKey);

        int[] maxPos = (int[]) input.get(maxPosKey);
        double[] baselines  = (double[]) input.get(baselineKey);

        //get array with time series
        double[] data       = (double[]) input.get(dataKey);
        double[] corrData   =  data.clone();

        //Marker
        IntervalMarker[] mWidth  = new IntervalMarker[npix];
        IntervalMarker[] mMaxPos = new IntervalMarker[npix];
        IntervalMarker[] mDeltat = new IntervalMarker[npix];
        IntervalMarker[] mEstArrivalTime = new IntervalMarker[npix];
        double[] mMaxAmplitude  = new double[data.length];
        double[] mAmplitudes    = new double[data.length];




        for(int pix = 0 ; pix < npix; pix++){
            int     lastSlice           = roi*(pix+1);
            int     firstSliceThresh    = roi*pix + (int)firstSlOverThresh[pix];



            //check if maxAmplitude fullfill saturation criterion
            if (saturationThreshold > data[pix * roi + maxPos[pix] ]){
                continue;
            }
            mMaxPos[pix] = new IntervalMarker(maxPos[pix], maxPos[pix] + 1);

            //get width
            int width = totArray[pix];
            mWidth[pix] = new IntervalMarker(firstSlOverThresh[pix],firstSlOverThresh[pix] + width);

            //estimate amplitude of saturated pulse (this formular is from MTreatSaturation.cc in Mars_Trunk Revision: 18096)
            // using an 4.polynom of the function
            double estAmplitude = (threshold - baselines[pix])/(0.898417 - 0.0187633*width + 0.000163919*width*width - 6.87369e-7*width*width*width + 1.13264e-9*width*width*width*width);

            //calculate time difference between first slice over Threshold and arrival time of pulse;
            double deltat    = -1.41371-0.0525846*width + 93.2763/(width+13.196);
            mDeltat[pix] = new IntervalMarker(maxPos[pix], maxPos[pix]+deltat);

            //estimate arrival time
            double estArrivalTime = firstSlOverThresh[pix] - deltat -1;
            mEstArrivalTime[pix] = new IntervalMarker(estArrivalTime, estArrivalTime + 1);

            // Loop over saturated slices and correct amplitudes
            for (int slice = firstSliceThresh;
                 slice < firstSliceThresh + totArray[pix] && slice < lastSlice;
                 slice++) {
                double t0  = slice - estArrivalTime - roi*pix;
                mMaxAmplitude[slice] = estAmplitude;
                Double amplitude = estAmplitude*(1-1/(1+exp(t0/2.14)))*exp(-t0/38.8)+baselines[pix];
                mAmplitudes[slice] = amplitude;
                if (amplitude > data[slice]){
                    corrData[slice] = amplitude;
                }
            }

        }
        input.put(outputKey + "_MaxAmplitude", mMaxAmplitude);
        input.put(outputKey + "_Amplitudes", mAmplitudes);
        input.put(outputKey + "_WidthMarker", mWidth);
        input.put(outputKey + "_MaxPosMarker", mMaxPos);
        input.put(outputKey + "_DeltaTMarker", mDeltat);
        input.put(outputKey + "_estAtMarker", mEstArrivalTime);
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
