package fact.photonstream.timeSeriesExtraction;

/**
 * The pulse shape of FACTs SiPMs. This class contains a single function returning
 * an array of amplitudes.
 */
public class TemplatePulse {

    /**
     * The FACT single p.e. pulse template at 2GHz sampling rate.
     * <p>
     * amplitude
     * |
     * 1.0 _|_        ___
     * |        /   \
     * |       /     |
     * |      |       \
     * |      |        \_
     * |      /          \_____
     * |     |                 \________
     * 0.0 _|_____|__________________________\___\ slices
     * |                              /
     * 0
     *
     * @param lengthInSlices The length of the template puls to be returned.
     * @return template
     * An array [lengthInSlices] with the amplitudes of the
     * template pulse.
     * <p>
     * amplitude
     * |
     * 1.0 _|_        ___
     * |        /   \
     * |       /     |
     * |      |       \
     * |      |
     * |      /
     * |     |
     * 0.0 _|_____|________|_____________________\ slices
     * |        |                     /
     * 0   lengthInSlices
     */
    public static double[] factSinglePePulse(int lengthInSlices) {

        final double periodeSliceInNs = 0.5;

        final double[] time = new double[lengthInSlices];
        for (int i = 0; i < time.length; i++) {
            time[i] = i * periodeSliceInNs;
        }

        double[] template = new double[lengthInSlices];
        for (int i = 0; i < time.length; i++) {

            final double amplitude =
                    1.626 *
                            (1.0 - Math.exp(-0.3803 * time[i]))
                            * Math.exp(-0.0649 * time[i]);

            if (amplitude < 0.0) {
                template[i] = 0.0;
            } else {
                template[i] = amplitude;
            }
        }
        return template;
    }
}
