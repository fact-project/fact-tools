package fact.utils;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;


import java.util.Arrays;

/**
 * Created by maxnoe on 08.08.16.
 */
public class DrsCellLastReadout implements StatefulProcessor {

    @Parameter(defaultValue = "eventTime", description = "Key to an event time in seconds (e.g. output of BoardTimeToSeconds)")
    private String timeKey = "eventTime";

    @Parameter(defaultValue = "StartCellData", description = "Key to the start cells")
    private String startCellKey = "StartCellData";

    @Parameter(defaultValue = "deltaT", description = "Key for the time since the last readout of the cells")
    private String outputKey = "deltaT";

    float[] lastReadOutTimes;

    private int numCells = 1024;

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, timeKey, startCellKey, "NROI");


        int roi = (int) item.get("NROI");
        short[] startCells = (short[]) item.get(startCellKey);
        float time = (float) item.get(timeKey);

        float[] deltaT = new float[Constants.NUMBEROFPIXEL * roi];

        for (int pixel=0; pixel < Constants.NUMBEROFPIXEL; pixel++) {
            for (int sample = 0; sample < roi; sample++) {
                int cell = Utils.sampleToCell(sample, startCells[pixel], numCells);
                deltaT[pixel * roi + sample] = time - this.lastReadOutTimes[pixel * numCells + cell];
                this.lastReadOutTimes[pixel * numCells + cell] = time;
            }
        }

        item.put(outputKey, deltaT);
        return item;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        this.lastReadOutTimes = new float[Constants.NUMBEROFPIXEL * numCells];
        Arrays.fill(this.lastReadOutTimes, Float.NaN);
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    public void setTimeKey(String timeKey) {
        this.timeKey = timeKey;
    }

    public void setStartCellKey(String startCellKey) {
        this.startCellKey = startCellKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
