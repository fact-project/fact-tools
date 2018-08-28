package fact.io.hdureader;

import fact.Constants;
import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The ZFitsStream can read FITS files containing raw data as recorded by the FACT telescope DAQ.
 * It also reads FITS files written by the Monte Carlo simulation program CERES.
 * It can decompress binary tables which have been compressed according to
 * the ZFITS standard. See http://arxiv.org/abs/1506.06045.
 * <p>
 * Created by mackaiver on 12/12/16.
 */
public class FITSStream extends AbstractStream {

    private short[] offsetCalibrationsConstants;
    private Reader reader;

    private HDU eventHDU;
    private Map<String, Serializable> fitsHeader = new HashMap<>();

    public String nameHDU = "Events";

    public FITSStream(SourceURL url) {
        this.url = url;
    }

    public FITSStream() {
    }

    @Override
    public void init() throws Exception {
        super.init();

        //create a fits object from the SourceURL this AbstractStream contains.
        //The FITs object does not know SourceURL so this fits reader is decoupled entirely from the streams framework
        URL url = new URL(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), this.url.getFile());
        FITS fits = new FITS(url);

        //get calibration constants they are stored in the first (and only) row of this hdu.
        Optional<HDU> offsetOptional = fits.getHDU("ZDrsCellOffsets");
        if (offsetOptional.isPresent()) {
            HDU offsetHDU = offsetOptional.get();
            Boolean ztable = offsetHDU.header.getBoolean("ZTABLE").orElse(false);
            Reader offsetReader;
            if (ztable) {
                offsetReader = ZFITSHeapReader.forTable(offsetHDU.getBinTable());
            } else {
                offsetReader = BinTableReader.forBinTable(offsetHDU.getBinTable());
            }

            OptionalTypesMap<String, Serializable> row;
            if (ztable) {
                //some of the ZDrsOffsetTables that were compressed have wrong tileheader ignore here
                row = ((ZFITSHeapReader) offsetReader).getNextRow(true);
            } else {
                row = offsetReader.getNextRow();
            }
            offsetCalibrationsConstants = row
                    .getShortArray("OffsetCalibration")
                    .orElseThrow(() -> new IOException("OffsetCalibration not found in file."));

        }
        // create a refrence to the events hdu
        eventHDU = fits.getHDU(nameHDU).orElseThrow(() -> new IOException("Inputfile did not contain HDU '" + nameHDU + "'"));

        BinTable eventsTable = eventHDU.getBinTable();

        // read each headerline and try to get the right datatype
        // from smallest to largest datatype
        // if no number can be found simply save the string.
        Header header = eventHDU.header;
        fitsHeader = header.asMapOfSerializables();

        Boolean ztable = eventHDU.header.getBoolean("ZTABLE").orElse(false);
        if (ztable) {
            reader = ZFITSHeapReader.forTable(eventsTable);
        } else {
            reader = BinTableReader.forBinTable(eventsTable);
        }

    }


    private void applyDrsOffsetCalib(int numSlices, short[] data, short[] startCellData, short[] calibrationConstants) {
        for (int ch = 0; ch < Constants.N_PIXELS; ch++) {
            int startCell = startCellData[ch];
            for (int sliceNum = 0; sliceNum < numSlices; sliceNum++) {
                int curCell = (startCell + sliceNum) % 1024;
                data[ch * numSlices + sliceNum] += calibrationConstants[ch * 1024 + curCell];
            }
        }
    }

    @Override
    public Data readNext() throws Exception {

        if (!reader.hasNext()) {
            return null;
        }

        OptionalTypesMap<String, Serializable> nextRow = reader.getNextRow();


        if (offsetCalibrationsConstants != null) {
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

    /**
     * Skips the given amount of rows in the data table.
     *
     * @param amount The amount of Rows to skip.
     * @throws IOException
     */
    public void skipRows(int amount) throws IOException{
        reader.skipRows(amount);
    }
}
