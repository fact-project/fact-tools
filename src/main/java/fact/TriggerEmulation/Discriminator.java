package fact.TriggerEmulation;

import fact.Constants;

public class Discriminator {

    public static int default_slice = 0;

    /**
     *Discriminate the signal of a given patch
     * @param data timeseries, array[n_pixels_pe_patch]
     * @param threshold threshold of the discriminator in DAC
     * @param minTimeOverThreshold minimum time the signal has to stay above the threhold
     * @param skipFirst number of slices to ignore at the beginning of the time series
     * @param skipLast number of slices to ignore at the end of the time series
     */
    public static int discriminatePatch(
            double[] data,
            int threshold,
            int minTimeOverThreshold,
            int skipFirst,
            int skipLast
    ) {
        int counter = 0;
        int patchTriggerSlice = default_slice;

        double thresholdInMillivolt = thresholdDACToMillivolt(threshold, Constants.MILLIVOLT_PER_DAC);

        for (int slice = skipFirst; slice < data.length-skipLast; slice++) {
            double slice_amplitude = data[slice];

            if (slice_amplitude >= thresholdInMillivolt){
                if (counter == 0){
                    patchTriggerSlice = slice;
                }
                counter++;
            }
            else if (slice_amplitude < thresholdInMillivolt){
                counter = 0;
            }
            if (counter >= minTimeOverThreshold){
                return patchTriggerSlice;
            }
        }
        return default_slice;
    }

    /**
     * loop over pixels patch by patch and discriminate each patch
     * @param data array[n_patches][n_pixels_pe_patch]
     * @param n_patches number of patches to loop over
     * @param patchTriggerSlice output array[n_patches][n_pixels_pe_patch] for triggered slices
     * @param threshold threshold of the discriminator in DAC
     * @param minTimeOverThreshold minimum time the signal has to stay above the threhold
     * @param skipFirst number of slices to ignore at the beginning of the time series
     * @param skipLast number of slices to ignore at the end of the time series
     * @return
     */
    public static boolean[] discriminatePatches(
            double[][] data,
            int n_patches,
            int[] patchTriggerSlice,
            int threshold,
            int minTimeOverThreshold,
            int skipFirst,
            int skipLast)
    {
        boolean[] triggerPrimitives = new boolean[n_patches];

        for (int patch = 0; patch < n_patches; patch++) {
            triggerPrimitives[patch] = false;

            patchTriggerSlice[patch] =
                    discriminatePatch(
                            data[patch],
                            threshold,
                            minTimeOverThreshold,
                            skipFirst,
                            skipLast
                    );

            if (patchTriggerSlice[patch] > default_slice){
                triggerPrimitives[patch] = true;
            }
        }
        return triggerPrimitives;
    }

    /**
     * Convert threshold in DAC units to millivolt units
     * @param threshold
     * @param millivoltPerDAC
     * @return
     */
    public static double thresholdDACToMillivolt(int threshold, double millivoltPerDAC) {
        return millivoltPerDAC*threshold;
    }


    public static int booleanToInt(boolean value) {
        // Convert true to 1 and false to 0.
        if (value){
            return 1;
        }
        return 0;
    }
}
