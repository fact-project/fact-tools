package fact.TriggerEmulation;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class EmulateLogicTest {

    @Test
    public void getPatchesOfFTU() {
        Integer[] patch0 = {0,1,2,3};
        Integer[] patch39 = {156,157,158,159};
        assertArrayEquals(EmulateLogic.getPatchesOfFTU(0).toArray(), patch0);
        assertArrayEquals(EmulateLogic.getPatchesOfFTU(39).toArray(), patch39);
    }

    @Test
    public void insertToArrayListSorted() {
        Integer[] list = {0,1,2,3};

        ArrayList<Integer> arraylist = new ArrayList<>();

        EmulateLogic.insertToArrayListSorted(3, arraylist);
        EmulateLogic.insertToArrayListSorted(0, arraylist);
        EmulateLogic.insertToArrayListSorted(2, arraylist);
        EmulateLogic.insertToArrayListSorted(1, arraylist);

        assertArrayEquals(arraylist.toArray(), list);
    }
}