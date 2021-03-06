package fact.TriggerEmulation;

import fact.Constants;
import fact.Utils;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Emulate the n-out-of-4 trigger logic of the trigger unit
 * and the n-out-of-40 logic of the trigger master
 * by applying it to the array of trigger primitives.
 *
 *  ﻿A trigger decision is taken when N out of the 40 primitives
 *  * have a rising edge within a time-window adjustable from 8 ns to 68 ns in steps of 4 ns.
 *
 *  * Created by jbuss on 26.08.18.
 */
public class EmulateLogic implements Processor {

    static Logger log = LoggerFactory.getLogger(EmulateDiscriminator.class);

    @Parameter(required = false, description = "decision of the logic [bool]")
    public String outKey;

    @Parameter(required = false,
            description = "boolean array [number of patches] flagging if patch triggered ")
    public String primitivesKey = "TriggerPrimitives";

    @Parameter(required = false,
            description = "int array [number of patches] containing each the first slice above threshold ")
    public String triggerSliceKey = "TriggerSlice";

    @Parameter(required = false,
            description = "minimum number of trigger patches per trigger unit to have a signal above threshold")
    public int nOutOf4 = 1;

    @Parameter(required = false,
            description = "minimum number of trigger units to have a signal above threshold")
    public int nOutOf40 = 1;

    @Parameter(required = false,
            description = "size of the time window within which the rising edges of the triggerprimitives should be")
    public int timeWindowSize = 12;

    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, primitivesKey, boolean[].class);
        Utils.isKeyValid(item, triggerSliceKey, int[].class);

        boolean[] triggerPrimitives = (boolean[]) item.get(primitivesKey);
        int[] patchTriggerSlice = (int[]) item.get(triggerSliceKey);

        boolean triggerDecision = hasTriggered(
                triggerPrimitives,
                patchTriggerSlice,
                nOutOf4,
                nOutOf40,
                timeWindowSize);
        item.put(outKey, triggerDecision);

        return item;
    }

    public static ArrayList<Integer> getPatchesOfFTU(int ftu_id)
    {
        ArrayList<Integer> ftus = new ArrayList<>();
        int first_patch_of_ftu = ftu_id*Constants.N_PATCHES_FTU;
        for (int patch = first_patch_of_ftu; patch < first_patch_of_ftu+Constants.N_PATCHES_FTU; patch++) {
            ftus.add(patch);
        }
        return ftus;
    }

    /**
     * Iterate over patches and decide weather the event has triggered for the given trigger properties
     * @param triggerPrimitives Array with discriminator trigger decisions
     * @param patchTriggerSlice Array with discriminator trigger times
     * @param nOutOf4 numper of necessary trigger patches to have a signal
     * @param nOutOf40 numper of necessary trigger units to have a signal
     * @param timeWindowSize duration of coincidence of the triggered patches
     * @return
     */
    public static boolean hasTriggered(
            boolean[] triggerPrimitives,
            int[] patchTriggerSlice,
            int nOutOf4,
            int nOutOf40,
            int timeWindowSize
    ) {
        ArrayList<Integer> triggerTimes = new ArrayList<>();

        int n_units = 0;
        for (int ftu = 0; ftu < Constants.N_FTUS; ftu++) {
            int n_trigger_patches = 0;

            for (Integer patch :
                    getPatchesOfFTU(ftu)) {

                if (triggerPrimitives[patch] && patchTriggerSlice[patch] > 0){
                    insertToArrayListSorted(patchTriggerSlice[patch], triggerTimes);
                    n_trigger_patches++;
                }
                if (n_trigger_patches >= nOutOf4){
                    n_units++;
                    break;
                }
            }
            if (n_units >= nOutOf40){
                if (hasCoincidentTriggers(triggerTimes, nOutOf40, timeWindowSize)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * sorted insert of int value into an int array
     * @param triggerSlicesToAdd
     * @param triggerSlicesArrayList int ArrayList holding trigger times added so far
     */
    public static void insertToArrayListSorted(int triggerSlicesToAdd, ArrayList<Integer> triggerSlicesArrayList) {
        int pos = Collections.binarySearch(triggerSlicesArrayList, triggerSlicesToAdd);
        if (pos < 0) {
            triggerSlicesArrayList.add(-pos-1, triggerSlicesToAdd);
        } else {
            throw new ValueException("Found invalid position (pos>=0) to insert value in array ");
        }
    }

    /**
     * Test for nOutOf40 coincident triggerPrimitives in the given time window
     * @param triggerSlicesSorted
     * @param nOutOf40
     * @param timeWindowSize
     * @return
     */
    public static boolean hasCoincidentTriggers(
                                                  ArrayList<Integer> triggerSlicesSorted,
                                                  int nOutOf40, 
                                                  int timeWindowSize
                                                ) {
        for (int i = 0; i <= triggerSlicesSorted.size() - nOutOf40; i++) {
            /**TODO: The coincidence does not distinguish between patches from the same and different FTUs,
             * so it may happen that pathces from the same FTU contribute to the coincidence. I don't know
             *  how the real trigger is handling it. However, since is currently only operating with a 1-OutOf-4 FTU
             *  and a 1-OutOf40 FTM logic this case is never met.
            **/
            if (nOutOf40 > 1) {
                throw new ValueException("Currently only nOutOf40 <= 1 is implemented correctly for the coincidence check");
            }
            int currTimeSpann = triggerSlicesSorted.get(i+nOutOf40-1) - triggerSlicesSorted.get(i);
            if (currTimeSpann < timeWindowSize){
                return true;
            }
        }
        return false;
    }
}

