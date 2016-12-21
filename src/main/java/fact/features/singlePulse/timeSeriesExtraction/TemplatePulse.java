package fact.features.singlePulse.timeSeriesExtraction;

/**
 * The pulse shape of FACTs SiPMs. This class contains a single function returning
 * an array of amplitudes.
 */
public class TemplatePulse {

    /**
    * The FACT single p.e. pulse template at 2GHz sampling rate.
    *
    *           amplitude
    *                |
    *           1.0 _|_        ___
    *                |        /   \
    *                |       /     |
    *                |      |       \
    *                |      |        \_
    *                |      /          \_____
    *                |     |                 \________
    *           0.0 _|_____|__________________________\___\ slices
    *                      |                              /
    *                      0
    *
    * @param lengthInSlices
    *           The length of the template puls to be returned.
    *
    * @return template
    *           An array [lengthInSlices] with the amplitudes of the
    *           template pulse.
    *
    *           amplitude
    *                |
    *           1.0 _|_        ___
    *                |        /   \
    *                |       /     |
    *                |      |       \
    *                |      |
    *                |      /
    *                |     |
    *           0.0 _|_____|________|_____________________\ slices
    *                      |        |                     /
    *                      0   lengthInSlices
    */
    public static double[] factSinglePePulse(int lengthInSlices) {

        final double[] time = timeSeries(lengthInSlices);

        final double[] template = new double[lengthInSlices];
        for (int i=0; i<time.length; i++) {
            final double amplitude = 
                1.626*
                (1.0-Math.exp(-0.3803*time[i]))
                *Math.exp(-0.0649*time[i]);
            
            if(amplitude < 0.0) {
                template[i] = 0.0;
            }else{
                template[i] = amplitude;
            }
        }
        return template;
    }


    public static double [] performancePaper(int lengthInSlices) {

        final double[] time = timeSeries(lengthInSlices);
        // from the FACT performance paper:
        // Calibration and performance of the photon sensor
        // response of FACT — the first G-APD Cherenkov telescope
        // doi:10.1088/1748-0221/9/10/P10012
        // Equation 2.7 and Table 5:
        final double c = 1.57;
        final double t0 = 2.7;
        final double tau = 0.9;
        final double lambda = 19;

        final double normalization = 0.7707; // ensure the max pulse amplitude is 1.0

        final double[] template = new double[lengthInSlices];
        for (int i=0; i<time.length; i++) {
            template[i] = normalization * 
                c*(1.0 - 1.0/(1.0 + Math.exp((time[i] - t0)/tau))) *  
                Math.exp(-((time[i] - t0)/lambda));
        }
        return template;        
    }

    public static double [] timeSeries(int lengthInSlices) {
        final double slicePeriodeInNs = 0.5;
        final double[] time = new double[lengthInSlices];
        for (int i=0; i<time.length; i++) {time[i] = i*slicePeriodeInNs;}
        return time;   
    }
}