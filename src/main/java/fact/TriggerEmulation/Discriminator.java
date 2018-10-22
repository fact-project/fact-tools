package fact.TriggerEmulation;

import com.google.common.primitives.Booleans;
import fact.Constants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

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
     *Compute the first occurence of a signal that is above a given {@code thresholdInDAC} and stays above it for
     * a requested time ({@code minTimeOverThreshold}).
     *
     * @param data timeseries
     * @param thresholdInDAC thresholdInDAC of the discriminator in DAC
     * @param minTimeOverThreshold minimum time (in unit slices) the signal has to stay above the threhold
     * @param skipFirst number of slices to ignore at the beginning of the time series
     * @param skipLast number of slices to ignore at the end of the time series
     * @return a DiscriminatorOutputObject
     */
    public static DiscriminatorOutput discriminatePatch(
            double[] data,
            int thresholdInDAC,
            int minTimeOverThreshold,
            int skipFirst,
            int skipLast
    ) {
        int timeOverThreshold = 0;
        int patchTriggerSlice = default_slice;

        double thresholdInMilliVolt = dacToMillivolt(thresholdInDAC);

        for (int slice = skipFirst; slice < data.length-skipLast; slice++) {
            double slice_amplitude = data[slice];

            if (slice_amplitude >= thresholdInMilliVolt){
                if (timeOverThreshold == 0){
                    patchTriggerSlice = slice;
                }
                timeOverThreshold++;
            }
            else if (slice_amplitude < thresholdInMilliVolt){
                timeOverThreshold = 0;
            }
            if (timeOverThreshold >= minTimeOverThreshold){
                return new DiscriminatorOutput(patchTriggerSlice, true);
            }
        }
        return new DiscriminatorOutput(patchTriggerSlice, false);
    }

    /**
     * loop over pixels patch by patch and discriminate each patch
     * @param data array with dimensions [n_patches][number_of_slices]
     * @param threshold threshold of the discriminator in DAC
     * @param minTimeOverThreshold minimum time the signal has to stay above the threhold
     * @param skipFirst number of slices to ignore at the beginning of the time series
     * @param skipLast number of slices to ignore at the end of the time series
     * @return array of DiscriminatorOutputObjects
     */
    public static DiscriminatorOutput[] discriminatePatches(
            double[][] data,
            int threshold,
            int minTimeOverThreshold,
            int skipFirst,
            int skipLast)
    {

        DiscriminatorOutput[] results = new DiscriminatorOutput[data.length];

        for (int patch = 0; patch < data.length; patch++) {
            results[patch] =
                    discriminatePatch(
                            data[patch],
                            threshold,
                            minTimeOverThreshold,
                            skipFirst,
                            skipLast
                    );
        }
        return results;
    }

    /**
     * Convert threshold in DAC units to millivolt units
     * @param amplitude in DAC units
     * @return amplitude in Millivolts
     */
    public static double dacToMillivolt(int amplitude) {
        return Constants.MILLIVOLT_PER_DAC*amplitude;
    }

    /**
     * Convert amplitude in DAC units to millivolt units
     * @param amplitude in millivolt units
     * @return amplitude in DAC units
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

    /***
     * Get the triggerSlice members from an array of DiscriminatorOutputs and return them as native int array
     * @param discriminatorOutputs array of DiscriminatorOutputs
     * @return boolean array with trigger slices from DiscriminatorOutput objects
     */
    public static int[] discriminatorOutputsToTriggerSliceArray(DiscriminatorOutput[] discriminatorOutputs) {
        return Arrays.stream(discriminatorOutputs)
                .mapToInt(p -> p.triggerSlice).toArray();
    }

    /***
     * Get the triggerPrimitive members from an array of DiscriminatorOutputs and return them as native boolean array
     * @param discriminatorOutputs array of DiscriminatorOutputs
     * @return boolean array with trigger primitives from DiscriminatorOutput objects
     */
    public static boolean[] discriminatorOutputsToTriggerPrimitiveArray(DiscriminatorOutput[] discriminatorOutputs) {

        return Booleans.toArray(
                Arrays.stream(discriminatorOutputs)
                        .map(p -> p.triggerPrimitive).collect(Collectors.toList())
        );
    }

    /**
     * A simple class to hold the result of the discriminator
     */
    public static class DiscriminatorOutput implements Serializable {
        public final int triggerSlice;
        public final boolean triggerPrimitive;

        public DiscriminatorOutput(int triggerSlice, boolean triggerPrimitive) {
            this.triggerSlice = triggerSlice;
            this.triggerPrimitive = triggerPrimitive;
        }
    }
}
