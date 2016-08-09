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

    double[][] lastReadOutTimes;

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, timeKey, startCellKey, "NROI");


        int roi = (int) item.get("NROI");
        short[] startCells = (short[]) item.get(startCellKey);
        double time = (double) item.get(timeKey);

        double[][] deltaT = new double[Constants.NUMBEROFPIXEL][roi];

        for (int pixel=0; pixel < Constants.NUMBEROFPIXEL; pixel++) {
            for (int sample = 0; sample < roi; sample++) {
                int cell = Utils.sampleToCell(sample, startCells[pixel], 1024);
                deltaT[pixel][sample] = time - lastReadOutTimes[pixel][cell];
                lastReadOutTimes[pixel][cell] = time;
            }
        }

        item.put(outputKey, deltaT);
        return item;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        lastReadOutTimes = new double[Constants.NUMBEROFPIXEL][1024];
        for (double[] row: lastReadOutTimes) {
            Arrays.fill(row, Double.NaN);
        }
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
