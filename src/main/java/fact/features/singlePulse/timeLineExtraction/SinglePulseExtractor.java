package fact.features.singlePulse.timeLineExtraction;

import com.google.common.math.DoubleMath;
import fact.Utils;

import java.util.ArrayList;

/**
 * The single pulse extractor by sebastian mueller acht.
 * It extracts the arrival times of single photons recorded by the SiPMs.
 */
public class SinglePulseExtractor {

    public static class Config {
        public int pulseToLookForLength;
        public int offsetSlices;
        public int negativePulseLength;
        public double factSinglePeAmplitudeInMv;
        public int maxIterations;

        public Config() {
            pulseToLookForLength = 20;
            offsetSlices = 7;
            negativePulseLength = 300;
            factSinglePeAmplitudeInMv = 10.0;
            maxIterations = 250;
        }       
    }

    public final Config config;

    public final double[] pulseToLookFor;
    public final double pulseToLookForIntegral;

    public final double[] plateauToLookFor;
    public final double plateauIntegral;

    public final double[] negativePulse;

    public SinglePulseExtractor(Config config) {
        this.config = config;

        // PulseToLookFor
        // --------------
        double[] pulse = new double[
            config.pulseToLookForLength + config.offsetSlices];

        AddFirstArrayToSecondArray.at(
            TemplatePulse.factSinglePePulse(config.pulseToLookForLength),
            pulse,
            config.offsetSlices
        );
        double sum = 0.0;
        for (double slice: pulse){
            sum += slice;
        }
        pulseToLookForIntegral = sum;
        pulseToLookFor = ElementWise.multiply(pulse, 1.0/pulseToLookForIntegral);

        // PlateauToLookFor
        // ----------------
        double[] plateau = new double[
            config.pulseToLookForLength + config.offsetSlices];

        double plateau_sum = 0.0;
        for (int i=0; i<config.offsetSlices; i++) {
                plateau[i] = 1.0;
                plateau_sum += plateau[i];
        }
        plateauIntegral = plateau_sum;
        plateauToLookFor = ElementWise.multiply(plateau, 1.0/plateauIntegral);

        // PulseToSubtract
        // ---------------
        double[] pulseToSubtract = TemplatePulse.factSinglePePulse(
            config.negativePulseLength);
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
     */
    public int[] getArrivalSlicesOnTimeline(double[] timeLine) {
        ArrayList<Integer> arrival_slices = new ArrayList<Integer>();
        int iteration = 0;

        while(iteration < config.maxIterations) {

            final double[] pulseResponse = Convolve.firstWithSecond(
                timeLine, 
                pulseToLookFor);

            final double[] baselineResponse = Convolve.firstWithSecond(
                timeLine,
                plateauToLookFor);

            final double[] response = ElementWise.subtractFirstFromSecond(
                baselineResponse,
                pulseResponse); 

            final ArgMax am = new ArgMax(response);
            final int maxSlice = am.arg + config.offsetSlices;
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
}