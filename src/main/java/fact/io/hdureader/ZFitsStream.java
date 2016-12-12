package fact.io.hdureader;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

/**
 * Created by mackaiver on 12/12/16.
 */
public class ZFitsStream extends AbstractStream {

    private short[] offsetCalibrationsConstants;
    private ZFitsHeapReader zFitsHeapReader;

    private Fits fits;
    private HDU eventHDU;

    public ZFitsStream(SourceURL url){
        this.url = url;
    }

    @Override
    public void init() throws Exception {
        super.init();

        //create a fits object
        URL url = new URL(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), this.url.getFile());
        fits = new Fits(url);


        //get calibration constants
        HDU offsetHDU = fits.getHDU("ZDrsCellOffsets");
        OptionalTypesMap<String, Serializable> row = ZFitsHeapReader.forTable(offsetHDU.getBinTable()).getNextRow();

        offsetCalibrationsConstants = row
                .getShortArray("OffsetCalibration")
                .orElseThrow(() -> new IOException("OffsetCalibration not found in file."));


        //create a refrence to the events hdu
        eventHDU = fits.getHDU("Events");
        BinTable eventsTable = eventHDU.getBinTable();

        zFitsHeapReader = ZFitsHeapReader.forTable(eventsTable);
    }


    private void applyDrsOffsetCalib(int numSlices, short[] data, short[] startCellData, short[] calibrationConstants) throws IllegalArgumentException {
        int numChannel = 1440;

        for (int ch=0; ch<numChannel; ch++) {
            // if the startCellData[ch] is negative ignore the calibration step for the channel
//            if (startCellData[ch]<0) {
////                log.warn("Start Cell for channel : " + ch + " is negative");
//                continue;
//            }
            //get the startCell
            int startCell = startCellData[ch];
            for (int sliceNum=0; sliceNum<numSlices; sliceNum++) {
                // the cells we look at are going roundabout
                int curCell = (startCell+sliceNum)%1024;

                data[ch*numSlices+sliceNum] += calibrationConstants[ ch*1024 + curCell ];
            }
        }
    }

    @Override
    public Data readNext() throws Exception {

        if(!zFitsHeapReader.hasNext()){
            return null;
        }

        OptionalTypesMap<String, Serializable> nextRow = zFitsHeapReader.getNextRow();

        short[] data = nextRow.getShortArray("Data").orElseThrow(() -> new IOException("Data not found in file."));
        short[] startCellData = nextRow.getShortArray("StartCellData").orElseThrow(() -> new IOException("StartCellData not found in file."));

        Integer roi = eventHDU.header.getInt("NROI").orElse(300);

        applyDrsOffsetCalib(roi, data, startCellData, offsetCalibrationsConstants );

        nextRow.put("Data", data);

        return DataFactory.create(nextRow);
    }
}
