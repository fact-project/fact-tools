package fact.io.hdureader;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The ZFitsStream can read FITS files containing raw data as recorded by the FACT telescope DAQ.
 * It also reads FITS files written by the Monte Carlo simulation program CERES.
 * It can decompress binary tables which have been compressed according to
 * the ZFITS standard. See http://arxiv.org/abs/1506.06045.
 *
 * Created by mackaiver on 12/12/16.
 */
public class FITSStream extends AbstractStream {

    private short[] offsetCalibrationsConstants;
    private Reader reader;

    private HDU eventHDU;
    private Map<String, Serializable> fitsHeader = new HashMap<>();

    public String nameHDU="Events";

    public FITSStream(SourceURL url){
        this.url = url;
    }

    public FITSStream() {    }

    @Override
    public void init() throws Exception {
        super.init();

        //create a fits object from the SourceURL this AbstractStream contains.
        //The FITs object does not know SourceURL so this fits reader is decoupled entirely from the streams framework
        URL url = new URL(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), this.url.getFile());
        FITS fits = new FITS(url);

        //get calibration constants they are stored in the first (and only) row of this hdu.
        HDU offsetHDU = fits.getHDU("ZDrsCellOffsets");
        if (offsetHDU != null){
            ZFITSHeapReader reader = ZFITSHeapReader.forTable(offsetHDU.getBinTable());
            boolean ignoreWrongTileHeader;
            OptionalTypesMap<String, Serializable> row = reader.getNextRow(ignoreWrongTileHeader=true);

            offsetCalibrationsConstants = row
                    .getShortArray("OffsetCalibration")
                    .orElseThrow(() -> new IOException("OffsetCalibration not found in file."));

        }
        //create a refrence to the events hdu
        eventHDU = fits.getHDU(nameHDU);
        if (eventHDU!=null) {
            BinTable eventsTable = eventHDU.getBinTable();

            //read each headerline and try to get the right datatype
            //from smallest to largest datatype
            //if no number can be found simply save the string.
            Header header = eventHDU.header;
            fitsHeader = header.asMapOfSerializables();

            Boolean ztable = eventHDU.header.getBoolean("ZTABLE").orElse(false);
            if (ztable) {
                reader = ZFITSHeapReader.forTable(eventsTable);
            } else {
                reader = BinTableReader.forBinTable(eventsTable);
            }
        }
    }



    private void applyDrsOffsetCalib(int numSlices, short[] data, short[] startCellData, short[] calibrationConstants){
        int numChannel = 1440;

        for (int ch=0; ch<numChannel; ch++) {
            int startCell = startCellData[ch];
            for (int sliceNum=0; sliceNum<numSlices; sliceNum++) {
                int curCell = (startCell+sliceNum)%1024;
                data[ch*numSlices+sliceNum] += calibrationConstants[ ch*1024 + curCell ];
            }
        }
    }

    @Override
    public Data readNext() throws Exception {

        if(!reader.hasNext()){
            return null;
        }

        OptionalTypesMap<String, Serializable> nextRow = reader.getNextRow();



        if (offsetCalibrationsConstants !=  null) {
            short[] data = nextRow.getShortArray("Data").orElseThrow(() -> new IOException("Data not found in file."));
            short[] startCellData = nextRow.getShortArray("StartCellData").orElseThrow(() -> new IOException("StartCellData not found in file."));

            Integer roi = eventHDU.header.getInt("NROI").orElse(300);

            applyDrsOffsetCalib(roi, data, startCellData, offsetCalibrationsConstants);
            nextRow.put("Data", data);
        }

        Data item = DataFactory.create(nextRow);
        item.putAll(fitsHeader);

        return item;
    }
}
