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
    public static final double[] plateauToLookFor;
    public static final double plateauIntegral;


    public static final int negativePulseLength = 300;
    public static final double[] negativePulse;
    /** The template time line of the pulse to be 
    *   subtracted from the time line. For FACT, this 
    *   subtraction pulse should be the full pulse with its 
    *   long falling edge. 300 slices = 150ns.
    *   Amplitude of the single puls is normalized to 1.0.
    */

    public static final int offsetSlices = 7;
    public static final double factSinglePeAmplitudeInMv = 10.0;

    static {
        // PulseToLookFor
        // --------------
        double[] pulse = new double[pulseToLookForLength+offsetSlices];
        AddFirstArrayToSecondArray.at(
            TemplatePulse.factSinglePePulse(pulseToLookForLength),
            pulse,
            offsetSlices
        );
        pulseToLookFor = pulse;

        double sum = 0.0;
        for (double slice : pulseToLookFor){
            sum += slice;
        }
        pulseToLookForIntegral = sum;

        // PlateauToLookFor
        // ----------------
        double[] plateau = new double[pulseToLookForLength+offsetSlices];
        double plateau_sum = 0.0;
        for (int i=0; i<offsetSlices; i++) {
                plateau[i] = 1.0;
                plateau_sum += plateau[i];
        }
        plateauToLookFor = plateau;
        plateauIntegral = plateau_sum;

        // PulseToSubtract
        // ---------------
        double[] pulseToSubtract = TemplatePulse.factSinglePePulse(
            negativePulseLength);
        negativePulse = ElementWise.multiply(pulseToSubtract, -1.0);

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
     *           were subtracted.
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

        while(iteration < maxIterations) {

            final double[] pulseResponse = Convolve.firstWithSecond(
                timeLine, 
                pulseToLookFor);

            final double[] baselineResponse = Convolve.firstWithSecond(
                timeLine,
                plateauToLookFor);

            double response[] = new double[pulseResponse.length];
            for (int i=0; i<pulseResponse.length; i++) {
                response[i] = pulseResponse[i]/pulseToLookForIntegral - 
                    baselineResponse[i]/plateauIntegral;
            }

            final ArgMax am = new ArgMax(response);
            final int maxSlice = am.arg + offsetSlices;
            final double maxResponse = am.max;

            if(maxResponse > 0.65) {
                AddFirstArrayToSecondArray.at(
                    negativePulse, 
                    timeLine, 
                    maxSlice);

                if(maxSlice >= 1)
                    arrival_slices.add(maxSlice);
            }else{
                break;
            }
            iteration++;
        }
        return Utils.arrayListToInt(arrival_slices);
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