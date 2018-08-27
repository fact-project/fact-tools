package fact.container;

import java.io.Serializable;
import java.util.LinkedList;

public class PreviousEventInfoContainer implements Serializable {

    private static final long serialVersionUID = -5978346023443096691L;

    LinkedList<short[]> previousStartCells;
    LinkedList<short[]> previousStopCells;
    LinkedList<int[]> previousUnixTimes;

    int listSize;

    public PreviousEventInfoContainer() {
        previousStartCells = new LinkedList<short[]>();
        previousStopCells = new LinkedList<short[]>();
        previousUnixTimes = new LinkedList<int[]>();
        this.udpateListSize();
    }

    public void addNewInfo(short[] startCells, short[] stoppCells, int[] unixTime) {
        previousStartCells.addFirst(startCells);
        previousStopCells.addFirst(stoppCells);
        previousUnixTimes.addFirst(unixTime);
        this.udpateListSize();
    }

    public short[] getPrevStartCells(int entry) {
        return previousStartCells.get(entry);
    }

    public short[] getPrevStoppCells(int entry) {
        return previousStopCells.get(entry);
    }

    public int[] getPrevUnixTimeCells(int entry) {
        return previousUnixTimes.get(entry);
    }


    public void removeLastInfo() {
        previousStartCells.removeLast();
        previousStopCells.removeLast();
        previousUnixTimes.removeLast();
        this.udpateListSize();
    }

    public LinkedList<short[]> getPreviousStartCells() {
        return previousStartCells;
    }

    public void setPreviousStartCells(LinkedList<short[]> previousStartCells) {
        this.previousStartCells = previousStartCells;
        this.udpateListSize();
    }

    public LinkedList<short[]> getPreviousStopCells() {
        return previousStopCells;
    }

    public void setPreviousStopCells(LinkedList<short[]> previousStopCells) {
        this.previousStopCells = previousStopCells;
        this.udpateListSize();
    }

    public LinkedList<int[]> getPreviousUnixTimes() {
        return previousUnixTimes;
    }

    public void setPreviousUnixTimes(LinkedList<int[]> previousUnixTimes) {
        this.previousUnixTimes = previousUnixTimes;
        this.udpateListSize();
    }

    public int getListSize() {
        return listSize;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    private void udpateListSize() {
        listSize = previousStartCells.size();
        if (listSize != previousStopCells.size() || listSize != previousUnixTimes.size()) {
            throw new RuntimeException("Size of the three different previous event info lists is not identical:\n"
                    + "previousStartCells: " + listSize + "\t previousStopCells:" + previousStopCells.size()
                    + "\t previousUnixTimes:" + previousUnixTimes.size());
        }
    }


}
