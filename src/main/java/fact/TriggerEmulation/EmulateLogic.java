package fact.TriggerEmulation;

import fact.Constants;
import javafx.collections.transformation.SortedList;
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

    @Parameter(required = true, description = "boolean array [number of patches] flagging if patch triggered ")
    private String key;

    @Parameter(required = false, description = "decision of the logic [bool]")
    private String outKey;

    @Parameter(required = false,
            description = "int array [number of patches] containing each the first slice above threshold ")
    private String triggerSliceKey = "TriggerSlice";

    @Parameter(required = false,
            description = "minimum number of trigger patches per trigger unit to have a signal above threshold")
    private int nOutOf4 = 1;

    @Parameter(required = false,
            description = "minimum number of trigger units to have a signal above threshold")
    private int nOutOf40 = 1;

    @Parameter(required = false,
            description = "size of the time window within which the rising edges of the triggerprimitives should be")
    private int timeWindowSize = 12;

    @Override
    public Data process(Data item) {
        int n_patches = Constants.NUMBEROFPIXEL/9;

        boolean[] triggerPrimitives = (boolean[]) item.get(key);

        int[] patchTriggerSlice = (int[]) item.get(triggerSliceKey);

        ArrayList<Integer> triggerTimes = new ArrayList<>();

        boolean triggerDecision = false;
        int n_units = 0;
        for (int ftu = 0; ftu < 40; ftu++) {
            int n_trigger_patches = 0;
            for (int patch = ftu*4; (patch < 4+(ftu*4) || patch < Constants.NUMBEROFPIXEL); patch++) {
                if (triggerPrimitives[patch]){
                    insertToArrayListSorted(patchTriggerSlice[patch], triggerTimes);
                    n_trigger_patches++;
                }
                if (n_trigger_patches >= nOutOf4){
                    n_units++;
                    break;
                }
            }
            if (n_units >= nOutOf40){
                if (areEnoughTriggersInOneTimeWindow(triggerTimes)){
                    triggerDecision = true;
                    break;
                }
            }
        }
        item.put(outKey, triggerDecision);

        return item;
    }

    public void insertToArrayListSorted(int x, ArrayList list) {
        int pos = Collections.binarySearch(list, x);
        if (pos < 0) {
            list.add(-pos-1, x);
        }
    }

    public boolean areEnoughTriggersInOneTimeWindow(ArrayList<Integer> list){
        for (int i = 0; i < list.size() - 40; i++) {
            int currTimeSpann = list.get(i+39) - list.get(i);
            if (currTimeSpann < this.timeWindowSize){
                return true;
            }
        }
        return false;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutKey(String outKey) {
        this.outKey = outKey;
    }

    public void setTriggerSliceKey(String triggerSliceKey) {
        this.triggerSliceKey = triggerSliceKey;
    }

    public void setnOutOf4(int nOutOf4) {
        this.nOutOf4 = nOutOf4;
    }

    public void setnOutOf40(int nOutOf40) {
        this.nOutOf40 = nOutOf40;
    }

    public void setTimeWindowSize(int timeWindowSize) {
        this.timeWindowSize = timeWindowSize;
    }
}

