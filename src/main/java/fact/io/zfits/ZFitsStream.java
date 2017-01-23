package fact.io.zfits;


import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.util.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The ZfitsReader can read FITS files containing raw dat as recorded by the FACT telescope DAQ. It also reads FITS files written
 * by the Monte Carlo simulation program CERES. It can decompress binary tables which have been compressed according to the ZFITS
 * standard. See http://arxiv.org/abs/1506.06045.
 *
 */
@Deprecated
public class ZFitsStream extends AbstractStream{
    static Logger log = LoggerFactory.getLogger(ZFitsStream.class);

    @Parameter(required = false, description = "This value defines the size of the buffer of the BufferedInputStream", defaultValue = "8*1024")
    public int bufferSize = 2880;

    @Parameter(required = false, description = "This value defines which table of the ZFitsfile should be read.", defaultValue = "Events")
    public String tableName = "Events";

    private Data headerItem = DataFactory.create();


    public boolean applyOffsetCalibration = true;


    private ZFitsTable fitsTable = null;

    private TableReader tableReader = null;

    public short[] calibrationConstants =  null;

    public void applyDrsOffsetCalib(int numSlices, int numChannel, short[] data, short[] startCellData, short[] calibrationConstants) throws IllegalArgumentException {
        if (data==null || data.length != numSlices*numChannel)
            throw new IllegalArgumentException("The length of the data array is wrong.");
        if (startCellData==null || startCellData.length != numChannel)
            throw new IllegalArgumentException("The length of the startCellData should be the same as the number of Channel");
        if (calibrationConstants==null || calibrationConstants.length != 1024*numChannel)
            throw new IllegalArgumentException("The length of the calibData is not the same as 1024*numChannel");

        for (int ch=0; ch<numChannel; ch++) {
            // if the startCellData[ch] is negative ignore the calibration step for the channel
            if (startCellData[ch]<0) {
                log.warn("Start Cell for channel : " + ch + " is negative");
                continue;
            }
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
    public void init() throws Exception {
        super.init();
        this.count = 0L;
        log.info("Reading file: {}", this.url.getFile());
        File f = new File(this.url.getFile());
        if (!f.canRead()){
            log.error("Cannot read file. Wrong path? ");
            throw new FileNotFoundException("Cannot read file");
        }

        DataInputStream dataStream = new DataInputStream(new BufferedInputStream(url.openStream(), bufferSize));
        dataStream.mark(3000000);
        //read the header and output some information
        List<String> block = ZFitsUtil.readBlock(dataStream);
        FitsHeader header = new FitsHeader(block);
        if (header.getKeyValue("SIMPLE").equals("T")){
            log.debug("Header claims this file conforms to FITS standard");
        }
        if (header.getKeyValue("EXTEND").equals("T")){
            log.debug("This file may contain extensions");
        }

        //read the second header
        block = ZFitsUtil.readBlock(dataStream);
        FitsHeader secondHeader = new FitsHeader(block);
        if(secondHeader.check("ZTABLE", FitsHeader.ValueType.BOOLEAN, "T")){
            log.info("File is ZFITS compresssed.");
        }
        if (! secondHeader.check("PCOUNT", FitsHeader.ValueType.INT)){
            log.warn("Invalid header format in file. Trying to read anyway.");
        }
        if(secondHeader.check("EXTNAME", FitsHeader.ValueType.STRING, "ZDrsCellOffsets")){
            log.info("File contains ZDrsCellOffsets.");
            ZFitsTable offsetTable = new ZFitsTable(secondHeader);
            TableReader reader = BinTableReader.createTableReader(offsetTable, dataStream);
            byte[][] bytes = reader.readNextRow();

            ByteOrder order = offsetTable.isCompressed ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            Data offsetsItem =  readDataFromBytes(bytes, offsetTable, order);

            if (offsetsItem.containsKey("OffsetCalibration")){
                this.calibrationConstants = (short[]) offsetsItem.get("OffsetCalibration");
            } else {
                throw new RuntimeException("OffsetCalibration constants not found in data file.");
            }
        }


        //reset to old mark and read the event table
        dataStream.reset();
        this.fitsTable = ZFitsUtil.skipToTable(dataStream, this.tableName);
        this.headerItem = createHeaderItem(fitsTable);
        //create the reader
        this.tableReader = BinTableReader.createTableReader(this.fitsTable, dataStream);
    }

    private Data createHeaderItem(ZFitsTable fitsTable) {
        // create headerItem
        // add all key value pairs which are not the column information
        Data item = DataFactory.create();
        for (Map.Entry<String, FitsHeader.FitsHeaderEntry> entry : fitsTable.getFitsHeader().getKeyMap().entrySet()) {
            String key   = entry.getKey();
            String value = entry.getValue().getValue();

            //ignore several information about the columns
            if ( key_in_ignore_list(key) ){
                continue;
            }

            switch(entry.getValue().getType()) {
                case BOOLEAN:
                    if (value.equals("T"))
                        item.put(key, Boolean.TRUE);
                    else
                        item.put(key, Boolean.FALSE);
                    break;
                case FLOAT:
                    item.put(key, Float.parseFloat(value));
                    break;
                case INT:
                    // BUGFIX:
                    // In rare cases the following four header keys could exceed the limit of an Integer in Java. The reason is,
                    // that the values of this header keys are written as 4 Byte unsigned int, with a limit of 2^32=4294967296,
                    // but Java only supports signed Integers with a limit of 2^31-1=2147483647.
                    // Therefore the values of the two keys are parsed into a Long. This should be investigated further and a
                    // more appropriate solution should be found.
                    ArrayList<String> keysListWithLargeValues = new ArrayList<>();
                    keysListWithLargeValues.add("NTRGMISC");
                    keysListWithLargeValues.add("NTRGEXT1");
                    keysListWithLargeValues.add("NTRGTIM");
                    keysListWithLargeValues.add("NTRGPED");
                    if (keysListWithLargeValues.contains(key))
                    {
                        item.put(key, Long.parseLong(value));
                    }
                    else
                    {
                        item.put(key, Integer.parseInt(value));
                    }
                    break;
                case STRING:
                    item.put(key, value);
                    break;
                default:
                    break;
            }
        }
        //insert filename
        item.put("@source", this.url.getProtocol() + ":" + this.url.getPath());
        return item;
    }

    @Override
    public Data readNext() throws Exception {

        if (this.tableReader == null) {
            throw new NullPointerException("Didn't initialize the reader, should never happen.");
        }
        //get the next row of data. When its null the file has ended
        byte[][] dataRow = this.tableReader.readNextRow();
        if (dataRow == null) {
            log.info("File {} ended.", url.getFile());
            return null;
        }
        ByteOrder order = this.fitsTable.isCompressed ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        Data item = readDataFromBytes(dataRow, this.fitsTable, order);
        item.putAll(headerItem);

        if( this.fitsTable.isCompressed && (this.calibrationConstants != null)){
            Utils.mapContainsKeys(item, "Data", "StartCellData", "NROI", "NPIX");
            short[] data = ((short[])item.get("Data"));
            short[] startCellData = (short[])item.get("StartCellData");
            int roi = (Integer) item.get("NROI");
            int numberOfPixel = (Integer) item.get("NPIX");

            if (applyOffsetCalibration) {
                applyDrsOffsetCalib(roi, numberOfPixel, data, startCellData, this.calibrationConstants);
            }

            item.put("Data", data);
        }

        return item;
    }

    private Data readDataFromBytes(byte[][] dataRow, ZFitsTable table, ByteOrder byteOrder) throws ParseException {
        Data item = DataFactory.create();

        //read the desired columns
        for (int colIndex=0; colIndex<table.getNumCols(); colIndex++) {
            byte[] data = dataRow[colIndex];

            //insert it into the item with the correct format
            FitsTableColumn columnInfo = table.getColumns(colIndex);
            ByteBuffer buffer = ZFitsUtil.wrap(data);
            buffer.order(byteOrder);
            switch (columnInfo.getType()) {
                case BOOLEAN:
                    if (columnInfo.getNumEntries()==1) {
                        boolean b = data[0]!=0;
                        item.put(columnInfo.getId(), b);
                    } else {
                        boolean[] bArray = new boolean[columnInfo.getNumEntries()];
                        for (int i=0; i<bArray.length; i++)
                            bArray[i] = data[i]!=0;
                        item.put(columnInfo.getId(), bArray);
                    }
                    break;
                case BYTE:
                    if (columnInfo.getNumEntries()==1) {
                        byte b = data[0];
                        item.put(columnInfo.getId(), b);
                    } else {
                        byte[] bArray = new byte[columnInfo.getNumEntries()];
                        System.arraycopy(data, 0, bArray, 0, bArray.length);
                        item.put(columnInfo.getId(), bArray);
                    }
                    break;
                case SHORT:
                    if (columnInfo.getNumEntries()==1) {
                        short b = buffer.getShort();
                        item.put(columnInfo.getId(), b);
                    } else {
                        short[] shortArray = new short[columnInfo.getNumEntries()];
                        for (int i=0; i<shortArray.length; i++)
                            shortArray[i] = buffer.getShort();
                        item.put(columnInfo.getId(), shortArray);
                    }
                    break;
                case INT:
                    if (columnInfo.getNumEntries()==1) {
                        int b = buffer.getInt();
                        item.put(columnInfo.getId(), b);
                    } else {
                        int[] intArray = new int[columnInfo.getNumEntries()];
                        for (int i=0; i<intArray.length; i++)
                            intArray[i] = buffer.getInt();
                        item.put(columnInfo.getId(), intArray);
                    }
                    break;
                case LONG:
                    if (columnInfo.getNumEntries()==1) {
                        long b = buffer.getLong();
                        item.put(columnInfo.getId(), b);
                    } else {
                        long[] longArray = new long[columnInfo.getNumEntries()];
                        for (int i=0; i<longArray.length; i++)
                            longArray[i] = buffer.getLong();
                        item.put(columnInfo.getId(), longArray);
                    }
                    break;
                case DOUBLE:
                    if (columnInfo.getNumEntries()==1) {
                        double b = buffer.getDouble();
                        item.put(columnInfo.getId(), b);
                    } else {
                        double[] doubleArray = new double[columnInfo.getNumEntries()];
                        for (int i=0; i<doubleArray.length; i++)
                            doubleArray[i] = buffer.getDouble();
                        item.put(columnInfo.getId(), doubleArray);
                    }
                    break;
                case FLOAT:
                    if (columnInfo.getNumEntries()==1) {
                        float b = buffer.getFloat();
                        item.put(columnInfo.getId(), b);
                    } else {
                        float[] doubleArray = new float[columnInfo.getNumEntries()];
                        for (int i=0; i<doubleArray.length; i++)
                            doubleArray[i] = buffer.getFloat();
                        item.put(columnInfo.getId(), doubleArray);
                    }
                    break;
                case STRING:
                    String s = new String(buffer.array());
                    item.put(columnInfo.getId(), s);
                    break;
                default:
                    throw new ParseException("The type of a column is wrong, or could not be read.");
            }
        }

        return item;
    }

    public ZFitsStream(SourceURL url) throws Exception {
        super(url);
    }

    public ZFitsStream() {
        super();
    }

    //this helper function checks if the key from the hdu
    //is in a list of keys that shall be ignored for
    //further processing
    private Boolean key_in_ignore_list(String key) {

        ArrayList<String> ignoreStarting = new ArrayList<>();
        ignoreStarting.add("TFORM");
        ignoreStarting.add("ZFORM");
        ignoreStarting.add("TTYPE");
        ignoreStarting.add("ZCTYPE");
        ignoreStarting.add("PCOUNT");

        for (String ignorekey : ignoreStarting) {
            if (key.startsWith(ignorekey)) {
                return true;
            }
        }
        return false;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
