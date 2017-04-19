package fact.utils;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * For each slice in the data item, calculate the corresponding cell id of the DRS4.
 * Created by maxnoe on 11.08.16.
 */
public class CalculateDRSCells implements Processor {

    @Parameter(defaultValue = "StartCellData", description = "Key to the drs start cells")
    private String startCellKey = "StartCellData";

    @Parameter(defaultValue = "cellIDs", description = "outputKey for the DRS4 cell ids")
    private String outputKey = "cellIDs";

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, startCellKey, "NROI");
        int roi = (int) item.get("NROI");
        short[] startcells = (short[]) item.get(startCellKey);

        short[] cells = new short[Constants.NUMBEROFPIXEL * roi];

        for (int pixel=0; pixel < Constants.NUMBEROFPIXEL; pixel++){
            for(int sample=0; sample < roi; sample++){
                cells[pixel * roi + sample] = (short) Utils.sampleToCell(sample, startcells[pixel], 1024);
            }
        }
        item.put(outputKey, cells);

        return item;
    }

    public void setStartCellKey(String startCellKey) {
        this.startCellKey = startCellKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
