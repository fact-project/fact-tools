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
 * It needs the time over threshold (timeOverThresholdsKey) that was measured at an amplitude of 1800 mV
 * Currently the conversion from tot to amplitude works only for this threshold
 *
 * Created by jbuss on 28.01.15.
 */
public class CorrectSaturation implements Processor {
    @Parameter(required=false, description="Array containing the raw data", defaultValue="raw:dataCalibrated")
    private String dataKey = "raw:dataCalibrated";

    @Parameter(required=false, description="Target Array for the saturation corrected raw data", defaultValue="raw:dataCalibrated")
    private String outputKey = "raw:dataCalibrated";

    @Parameter(required=false, description="1440pix array containing the time-over-threshold for the pixel", defaultValue="pixels:timeOverThresholds")
    private String timeOverThresholdsKey = "pixels:timeOverThresholds";;

    @Parameter(required=false, description="1440pix array containing the first slice time-over-threshold for the pixel", defaultValue="pixels:firstSliceOverThresholds")
    private String firstSliceOverThresholdsKey = "pixels:firstSliceOverThresholds";
    
    @Parameter(required = false, defaultValue = "meta:timeOverThreshold:threshold")
	private String thresholdKey = "meta:timeOverThresholdProcessor:threshold";

    @Parameter(required=false, description="threshold that defines a pixel showing saturation", defaultValue="1900.0")
    private double saturationThreshold = 1900.0;

    @Parameter(required=false, description="1440pix array containing the slice of maximum amplitude", defaultValue="pixels:maxAmplitudePositions")
	protected String maxAmplitudePositionsKey = "pixels:maxAmplitudePositions";
    @Parameter(required=false, description="key for the baseline output, 1440 pixel array containing a baseline amplitude for each pixel", defaultValue="pixels:baselines")
    private String baselinesKey = "pixels:baselines";

    // Default values
    private Integer npix                = Constants.NUMBEROFPIXEL;
    private Integer roi                 = 300;

    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, "NPIX", Integer.class);
        Utils.mapContainsKeys(item, dataKey, timeOverThresholdsKey, maxAmplitudePositionsKey, baselinesKey, firstSliceOverThresholdsKey, "NPIX");

        npix        = (Integer) item.get("NPIX");
        roi         = (Integer) item.get("NROI");
        
        double threshold = (Double) item.get(thresholdKey);

        double[] firstSliceOverThresholds = (double[]) item.get(firstSliceOverThresholdsKey);
        int[] timeOverThresholds 	    = (int[]) item.get(timeOverThresholdsKey);

        int[] maxAmplitudePositions = (int[]) item.get(maxAmplitudePositionsKey);
        double[] baselines  = (double[]) item.get(baselinesKey);

        //get array with time series
        double[] data       = (double[]) item.get(dataKey);
        double[] corrData   =  data.clone();

        //Marker
        IntervalMarker[] markWidth          = new IntervalMarker[npix];
        IntervalMarker[] markMaxPos         = new IntervalMarker[npix];
        IntervalMarker[] markDeltaT         = new IntervalMarker[npix];
        IntervalMarker[] markEstArrivalTime = new IntervalMarker[npix];

        // ------------------------------------------------------------------------------------------------------------

        for(int pix = 0 ; pix < npix; pix++){
            int     firstSlice              = roi*pix;
            int     firstSliceAboveThresh   = (int)firstSliceOverThresholds[pix];
            int     maxPosInPix             = pix * roi + maxAmplitudePositions[pix];
            double  maxAmplitude            = data[maxPosInPix];

            //check if maxAmplitude fullfill saturation criterion
            if (saturationThreshold > maxAmplitude){
                continue;
            }
            markMaxPos[pix] = new IntervalMarker(maxAmplitudePositions[pix], maxAmplitudePositions[pix] + 1);

            //get the width of the pulse in order to compile the correction
            int width       = timeOverThresholds[pix];
            markWidth[pix]  = new IntervalMarker(firstSliceOverThresholds[pix],firstSliceOverThresholds[pix] + width);

            //estimate amplitude of saturated pulse (this formular is from MTreatSaturation.cc in Mars_Trunk Revision: 18096)
            // using an 4.polynom of the function
            double estimatedAmplitude = (threshold - baselines[pix]);
            estimatedAmplitude /= (0.898417 - 0.0187633*width + 0.000163919*pow(width,2) - 6.87369e-7*pow(width,3) + 1.13264e-9*pow(width,4));

            //calculate time difference between first slice over Threshold and arrival time of pulse;
            double deltaT       = -1.41371-0.0525846*width + 93.2763/(width+13.196);
            markDeltaT[pix]     = new IntervalMarker(maxAmplitudePositions[pix], maxAmplitudePositions[pix]+deltaT);

            //estimate arrival time
            double estimatedArrivalTime = firstSliceOverThresholds[pix] - deltaT -1;
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

        item.put(outputKey + "_WidthMarker", markWidth);
        item.put(outputKey + "_MaxPosMarker", markMaxPos);
        item.put(outputKey + "_DeltaTMarker", markDeltaT);
        item.put(outputKey + "_estAtMarker", markEstArrivalTime);
        item.put(outputKey, corrData);

        return item;
    }
}
