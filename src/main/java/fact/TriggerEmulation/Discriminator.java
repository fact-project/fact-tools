package fact.TriggerEmulation;

import fact.Constants;

/**
 * Class that implements a software addaption of a discriminator in the sense of
 * the device used on FACTs FTU (Trigger Unit) boards.
 * (Source: Design and operation of FACT - the first G-APD Cherenkov telescope,
 * DOI: 10.1088/1748-0221/8/06/P06008, P.20)
 * The class contains functions to digitize the signals of a given patch (or a bunch of them)
 * by comparing them to a given threshold.
 * Furhtermore it contains helper functions for the discrimination e.g. conversion between DAC and mV
 */
public class Discriminator {

    public static int default_slice = 0;

    /**
     * @param minTimeOverThreshold minimum time the signal has to stay above the threhold
     *Compute the first occurence of a signal that is above a given {@code thresholdInDAC} and stays above it for
     * a requested time ({@code minTimeOverThreshold}).
     * @param data timeseries
     * @param thresholdInDAC thresholdInDAC of the discriminator in DAC
     * @param skipFirst number of slices to ignore at the beginning of the time series
     * @param skipLast number of slices to ignore at the end of the time series
     */
    public static int discriminatePatch(
            double[] data,
            int thresholdInDAC,
            int minTimeOverThreshold,
            int skipFirst,
            int skipLast
    ) {
        int counter = 0;
        int patchTriggerSlice = default_slice;

        double thresholdInMilliVolt = dacToMillivolt(thresholdInDAC);

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
     * @param amplitude
     * @return
     */
    public static double dacToMillivolt(int amplitude) {
        return Constants.MILLIVOLT_PER_DAC*amplitude;
    }

    /**
     * Convert amplitude in DAC units to millivolt units
     * @param amplitude
     * @return
     */
    public static int millivoltToDAC(double amplitude) {
        long res = Math.round(amplitude/Constants.MILLIVOLT_PER_DAC);
        return (int) res;
    }


    public static int booleanToInt(boolean value) {
        // Convert true to 1 and false to 0.
        if (value){
            return 1;
        }
        return 0;
    }
}
