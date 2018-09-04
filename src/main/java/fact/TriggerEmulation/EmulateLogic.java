package fact.TriggerEmulation;

import fact.Constants;
import fact.Utils;
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

        boolean triggerDecision = isTriggerDecision(
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

    public static boolean isTriggerDecision(
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
                if (areEnoughTriggersInOneTimeWindow(triggerTimes, nOutOf40, timeWindowSize)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * sorted insert of int value into an int array
     * @param x
     * @param list
     */
    public static void insertToArrayListSorted(int x, ArrayList<Integer> list) {
        int pos = Collections.binarySearch(list, x);
        if (pos < 0) {
            list.add(-pos-1, x);
        }
    }

    /**
     * Check if nOutOf40 triggerPrimitives where in the given time window
     * @param list
     * @return
     */
    public static boolean areEnoughTriggersInOneTimeWindow(ArrayList<Integer> list,
                                                           int nOutOf40, int timeWindowSize){
        for (int i = 0; i <= list.size() - nOutOf40; i++) {
            /**TODO: This does not distinguish between patches from the same FTU, so it may happen that nOutOf40 or more
             * FTUs reply a positive trigger and less than nOutOf40 are in the same time window, but in case enough FTUs
             * have patches within right time window this would still lead to a positive overall trigger. I don'' know
             *  how the real trigger is handling it. However, since is currently only operating with a 1-OutOf-4 FTU
             *  and a 1-OutOf40 FTM logic this case is never met.
            **/
            int currTimeSpann = list.get(i+nOutOf40-1) - list.get(i);
            if (currTimeSpann < timeWindowSize){
                return true;
            }
        }
        return false;
    }
}

