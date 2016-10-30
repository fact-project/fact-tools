package fact.features.singlePulse.timeLineExtraction;

import com.google.common.math.DoubleMath;
import fact.Utils;

import java.util.ArrayList;

/**
 * The single pulse extractor by sebastian mueller acht.
 * It extracts the arrival times of single photons recorded by the SiPMs.
 */
public class SinglePulseExtractor {

    public static final int pulseToLookForLength = 20;
    public static final double[] pulseToLookFor;
    public static final double pulseToLookForIntegral;
    /** A template time line of the pulse to look for. 
    *   For FACT pulses, it turned out best to mostly use 
    *   the rising edge of a pulse up to the maximum and 
    *   only a small part of the falling edge, 
    *   20 slices = 10ns.
    *   Amplitude of the single puls ids normalized to 1.0.
    */

    public static final int negativePulseLength = 300;
    public static final double[] negativePulse;
    /** The template time line of the pulse to be 
    *   substracted from the time line. For FACT, this 
    *   substraction pulse should be the full pulse with its 
    *   long falling edge. 300 slices = 150ns.
    *   Amplitude of the single puls is normalized to 1.0.
    */

    public static final double factSinglePeAmplitudeInMv = 10.0;

    static {
        pulseToLookFor = TemplatePulse.factSinglePePulse(
            pulseToLookForLength);
        double[] pulseToSubstract = TemplatePulse.factSinglePePulse(
            negativePulseLength);
        negativePulse = ElementWise.multiply(pulseToSubstract, -1.0);

        double sum = 0.0;
        for (double slice : pulseToLookFor){
            sum += slice;
        }
        pulseToLookForIntegral = sum;
    }

    /**
     * Reconstructs the arrival slices of single photons on a timeline.
     *
     * @return arrival_slices
     *           A list of the arrival slices of photons found on
     *           the time line.
     *
     * @param timeLine
     *           The time line to look for pulses in. The time line
     *           is modified in place. When the extractor was
     *           successfull, the time line is flat and all pulses
     *           were substracted.
     *           Amplitude of the single puls must be normalized to 1.0.
     *
     * @param maxIterations
     *           The maximum iterations on a time line before abort.
     */
    public static int[] getArrivalSlicesOnTimeline(
        double[] timeLine, 
        int maxIterations
    ) {
        ArrayList<Integer> arrival_slices = new ArrayList<Integer>();
        int iteration = 0;

        subtractMinimum(timeLine);

        while(iteration < maxIterations) {
            final double[] conv = Convolve.firstWithSecond(
                timeLine, 
                pulseToLookFor);
            final ArgMax am = new ArgMax(conv);
            final int offsetSlices = 
                (int)((double)(pulseToLookFor.length)*0.35);
            // The offsetSlices are needed to comensate both the asymetric 
            // convolution and the asymetric amplitude distribution in the 
            // pulse template (mostly the rising edge of the pulse).
            // These asymetries cause the maximum amplitude in conv not to 
            // be the optimum position for the pulse substraction.
            // The offsetSlices are chosen to correct for this and as a 
            // first guide we provide here the magic factor: 
            // offsetSlices = 0.35*templatePulse.length
            // This indicates the dependency of offsetSlices of the 
            // templatePulse.length 
            // (offsetSlices = 7 for templatePulse.length = 20).
            // It might be that offsetSlices can be optimized based on the
            // maxResponse.
            final int maxSlice = am.arg - offsetSlices;
            final double maxResponse = am.max/pulseToLookForIntegral;

            if(maxResponse > 0.5) {
                AddFirstArrayToSecondArray.at(
                    negativePulse, 
                    timeLine, 
                    maxSlice);

//                applyAcCoupling(timeLine);
                arrival_slices.add(am.arg);
            }else{
                break;
            }
            iteration++;
        }
        return Utils.arrayListToInt(arrival_slices);
    }

    /**
     * Estimates the effect of FACT's AC coupling in between the
     * photo-electric converter (SIPM) and the signal sampler (DRS4).
     * Here simply the mean of the time line is substracted from it.
     *
     * @param timeLine
     *           The time line is modified inplace.
     */
    public static void applyAcCoupling(double[] timeLine) {
        if(timeLine.length == 0)
            return;

        double sum = 0.0;
        for (double slice : timeLine){
            sum += slice;
        }

        final double mean = sum/(double)(timeLine.length);

        for (int i = 0; i<timeLine.length; i++) {
            timeLine[i] = timeLine[i] - mean;
        }
    }

    /**
     * Subtract the minimum amplitude on a timeline from the whole timeline.
     *
     * @param timeLine
     *           The time line is modified inplace.
     */
    public static void subtractMinimum(double[] timeLine) {
        if(timeLine.length == 0)
            return;

        double min = timeLine[0];
        for (int i = 0; i<timeLine.length; i++) {
            if (timeLine[i] < min) {
                min = timeLine[i];
            }
        }

        for (int i = 0; i<timeLine.length; i++) {
            timeLine[i] = timeLine[i] - min;
        }
    }

    /**
     * Convert time line amplitudes from mV to normalized amplitudes of
     * single photon pulses.
     *
     * @return timeLineNormalizedSinglePulses
     *           The amplitude of single pulses is 1.0 here.
     *
     * @param timeLineInMv
     *           In milli Volts (single pulse amplitude about 10mV)
     */
    public static double[] milliVoltToNormalizedSinglePulse(
        double[] timeLineInMv
    ) {

        return ElementWise.multiply(
            timeLineInMv, 
            1.0/factSinglePeAmplitudeInMv);
    }
}