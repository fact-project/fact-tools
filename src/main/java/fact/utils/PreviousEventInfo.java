package fact.utils;

import fact.Utils;
import fact.container.PreviousEventInfoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


public class PreviousEventInfo implements Processor {
    static Logger log = LoggerFactory.getLogger(PreviousEventInfo.class);

    @Parameter(required = true)
    public String startCellKey = null;

    @Parameter(required = true)
    public String outputKey = null;

    @Parameter(required = false, description = "The key containing the UnixTimeUTC")
    public String unixTimeKey = "UnixTimeUTC";

    @Parameter(required = false, description = "Set the amount of events to buffer for the jumpremoval.")
    public int limitEvents = 20;

    PreviousEventInfoContainer previousEventInfo = new PreviousEventInfoContainer();


    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, startCellKey, short[].class);
        Utils.isKeyValid(item, "NROI", Integer.class);
        Utils.isKeyValid(item, unixTimeKey, int[].class);

        int[] eventTime = (int[]) item.get(unixTimeKey);
        short[] startCellArray = (short[]) item.get(startCellKey);
        int length = (Integer) item.get("NROI");

        short[] stopCellArray = new short[startCellArray.length];
        //calculate the stopcellArray for the current event
        for (int i = 0; i < startCellArray.length; ++i) {
            //there are 1024 capacitors in the ringbuffer
            stopCellArray[i] = (short) ((startCellArray[i] + length) % 1024);
        }

        previousEventInfo.addNewInfo(startCellArray, stopCellArray, eventTime);

        if (previousEventInfo.getListSize() > limitEvents) {
            previousEventInfo.removeLastInfo();
        }

        item.put(outputKey, previousEventInfo);
        return item;
    }
}
