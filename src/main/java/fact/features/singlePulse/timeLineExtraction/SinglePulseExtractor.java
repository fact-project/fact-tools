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
        /**
         * An input configurstion for a SinglePulseExtractor instance
         */
        public int pulseToLookForLength;
        public int plateauLength;
        public int negativePulseLength;
        public double factSinglePeAmplitudeInMv;
        public int maxIterations;

        public Config() {
            /**
             * Default values suitable for FACT's 2GHz standart DRS sampling.
             */
            pulseToLookForLength = 20;
            plateauLength = 7;
            negativePulseLength = 300;
            factSinglePeAmplitudeInMv = 10.0;
            maxIterations = 250;
        }       
    }

    public static class Result {
        public int[] pulseArrivalSlices;
        public double[] timeSeriesAfterExtraction;

        public int numberOfPulses() {
            return pulseArrivalSlices.length;
        }

        public double timeSeriesBaseLine() {
            double sum = 0.0;
            for(int i=0; i<timeSeriesAfterExtraction.length; i++)
                sum += timeSeriesAfterExtraction[i];
            return sum/timeSeriesAfterExtraction.length;
        }    
    }

    public Config config;

    public double[] pulseToLookFor;
    public double pulseToLookForIntegral;

    public double[] plateauToLookFor;
    public double plateauIntegral;

    public double[] negativePulse;

    public SinglePulseExtractor(Config config) {
        this.config = config;
        initPulseToLookFor();
        initPlateau();
        initNegativePulse();
    }

    void initPulseToLookFor() {
        double[] pulse = new double[
            config.pulseToLookForLength + config.plateauLength];

        AddFirstArrayToSecondArray.at(
            TemplatePulse.factSinglePePulse(config.pulseToLookForLength),
            pulse,
            config.plateauLength
        );
        double sum = 0.0;
        for (double slice: pulse){
            sum += slice;
        }
        pulseToLookForIntegral = sum;
        pulseToLookFor = ElementWise.multiply(pulse, 1.0/pulseToLookForIntegral);  
    }

    void initPlateau() {
        double[] plateau = new double[
            config.pulseToLookForLength + config.plateauLength];

        double plateau_sum = 0.0;
        for (int i=0; i<config.plateauLength; i++) {
                plateau[i] = 1.0;
                plateau_sum += plateau[i];
        }
        plateauIntegral = plateau_sum;
        plateauToLookFor = ElementWise.multiply(plateau, 1.0/plateauIntegral);
    }

    void initNegativePulse() {
        double[] pulseToSubtract = TemplatePulse.factSinglePePulse(
            config.negativePulseLength);
        negativePulse = ElementWise.multiply(pulseToSubtract, -1.0);        
    }

    /**
     * Reconstructs the arrival slices of single photons on a timeline.
     *
     * @return result
     *           A class containing:
     *           - pulseArrivalSlices
     *           - timeSeriesAfterExtraction
     *
     * @param timeLine
     *           The time line to look for pulses in. The time line
     *           is modified in place. When the extractor was
     *           successfull, the time line is flat and all pulses
     *           were subtracted.
     *           Amplitude of the single puls must be normalized to 1.0.
     */
    public Result extractFromTimeline(double[] timeLine) {
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
            final int maxSlice = am.arg + config.plateauLength;
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

        Result result = new Result();
        result.timeSeriesAfterExtraction = timeLine;
        result.pulseArrivalSlices = Utils.arrayListToInt(arrival_slices);
        return result;
    }
}