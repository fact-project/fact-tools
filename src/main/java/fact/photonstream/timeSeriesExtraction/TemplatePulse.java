package fact.photonstream.timeSeriesExtraction;

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
    public static double[] factSinglePePulse(int lengthInSlices, double samplingPeriodInNs) {

        final double[] time = new double[lengthInSlices];
        for (int i = 0; i < time.length; i++) {
            time[i] = i * samplingPeriodInNs;
        }

        double[] template = new double[lengthInSlices];
        for (int i = 0; i < time.length; i++) {

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

    public static double[] factSinglePePulse(int lengthInSlices) {

        final double samplingPeriodInNs = 0.5;
        return factSinglePePulse(lengthInSlices, samplingPeriodInNs);

    }

    public static double factSinglePePulseIntegral() {

        final double half_height = 0.5; // this is fix forever :-D
        final double samplingPeriodInNs = 1e-3;
        final int integrationWindowLengthInSlices = 30;
        final double sliceInNs = 0.5;
        final int factor = (int)(sliceInNs / samplingPeriodInNs);
        final double pulseLengthInNs = integrationWindowLengthInSlices/sliceInNs + 3.;
        final int templateLength = (int) (pulseLengthInNs / samplingPeriodInNs);
        double[] template = factSinglePePulse(templateLength, samplingPeriodInNs);

        // resample in 500ps steps, i.e. take only every 500th sample
        double[] sums = new double[factor];
        double[] resampled_pulse;
        for (int start_offset = 0; start_offset < factor; start_offset++){
            resampled_pulse = new double[templateLength/factor];
            for (int i=0; i<resampled_pulse.length; i++){
                resampled_pulse[i] = template[start_offset+(i*factor)];
            }

            sums[start_offset] = integrate(
                resampled_pulse,
                findHalfHeightPosition(resampled_pulse, 1.),
                integrationWindowLengthInSlices
            );
        }

        return mean(sums);

    }

    public static int findHalfHeightPosition(double[] samples, double height) {
        for (int i= 1; i < samples.length; i++){
            if (samples[i] > height/2.) {
                return i - 1;
            }
        }
        return samples.length;
    }

    public static double integrate(double[] samples, int start, int length) {
        double sum = 0;
        for (int i=0; i < length; i++) {
            sum += samples[start+i];
        }
        return sum;
    }

    public static double mean(double[] foo) {
        if (foo.length == 0) { return Double.NaN; }

        double m = 0.;
        for (double a: foo){
            m += a;
        }
        return m / foo.length;
    }

}