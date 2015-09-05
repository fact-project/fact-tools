package fact.io.zfits;


import fact.Utils;
import fact.io.FactStream;
import org.apache.commons.cli.MissingArgumentException;
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
import java.util.Map;

public class ZFitsStream extends AbstractStream implements FactStream{

    private File drsFile;
    private boolean hasReadCalibrationConstants = false;

    @Parameter(required = false, description = "This value defines the size of the buffer of the BufferedInputStream", defaultValue = "8*1024")
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }


    @Parameter(required = false, description = "This value defines which table of the ZFitsfile should be read.", defaultValue = "Events")
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    static Logger log = LoggerFactory.getLogger(ZFitsStream.class);
    private int bufferSize = 2880;

    private DataInputStream dataStream;
    private Data headerItem = DataFactory.create();
    private String tableName = "Events";
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    private ZFitsTable fitsTable = null;

    private TableReader tableReader = null;

    private short[] calibrationConstants;

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
        log.info("Read file: {}", this.url.getFile());
        File f = new File(this.url.getFile());
        if (!f.canRead()){
            log.error("Cannot read file. Wrong path? ");
            throw new FileNotFoundException("Cannot read file");
        }
        this.dataStream = new DataInputStream(new BufferedInputStream(getInputStream(), bufferSize ));

        //get calibration constants
        this.dataStream.mark(10000000);
        try {
            if(!this.hasReadCalibrationConstants) {
                this.calibrationConstants = readCalibrationConstants(this.url);
            }
        } catch (MissingArgumentException e){
            log.info("Reading standard .fits file.");
        }



        //reset to old mark and read the event table
        this.dataStream.reset();
        this.fitsTable = ZFitsUtil.skipToTable(this.dataStream, this.tableName);

        log.info("Found Table " + tableName);

        // create headerItem
        // add all key value pairs which are not the column information TODO finish extracting column information
        for (Map.Entry<String, FitsHeader.FitsHeaderEntry> entry : this.fitsTable.getFitsHeader().getKeyMap().entrySet()) {
            String key   = entry.getKey();
            String value = entry.getValue().getValue();

            //ignore several information about the columns
            if ( key_in_ignore_list(key) ){
                //log.info("ignore:" + key);
                continue;
            }

            switch(entry.getValue().getType()) {
                case BOOLEAN:
                    if (value.equals("T"))
                        this.headerItem.put(key, Boolean.TRUE);
                    else
                        this.headerItem.put(key, Boolean.FALSE);
                    break;
                case FLOAT:
                    this.headerItem.put(key, Float.parseFloat(value));
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
                        this.headerItem.put(key, Long.parseLong(value));
                    }
                    else
                    {
                        this.headerItem.put(key, Integer.parseInt(value));
                    }
                    break;
                case STRING:
                    this.headerItem.put(key, value);
                    break;
                default:
                    break;
            }
        }
        //insert filename
        this.headerItem.put("@source", this.url.getProtocol() + ":" + this.url.getPath());

        //create the reader
        this.tableReader = BinTableReader.createTableReader(this.fitsTable, this.dataStream);

        if (this.fitsTable.getCommpressed()) {
            this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else {
            this.byteOrder = ByteOrder.BIG_ENDIAN;
        }
    }

    private short[] readCalibrationConstants(SourceURL url) throws Exception {
        short[] constants;
        ZFitsStream drsStream = new ZFitsStream(url);
        drsStream.hasReadCalibrationConstants = true;
        drsStream.setTableName("ZDrsCellOffsets");
        try{
            drsStream.init();
        } catch (MissingArgumentException e) {
            log.error("No ZDrsCellOffsets found in file.");
            throw e;
        }
        Data item = drsStream.read();
        if (!item.containsKey("OffsetCalibration"))
            throw new NullPointerException("Missing OffsetCalibration");
        constants = (short[])item.get("OffsetCalibration");
        if (constants == null) {
            throw new NullPointerException("Should not happen");
        }
        return constants;
    }


    @Override
    public Data readNext() throws Exception {
        Data item = DataFactory.create(headerItem);

        if (this.tableReader == null)
            throw new NullPointerException("Didn't initialize the reader, should never happen.");
        //get the next row of data if zero we finished
        byte[][] dataRow = this.tableReader.readNextRow();
        if (dataRow == null)
            return null;

        item = readDataFromBytes(item, dataRow, this.fitsTable, this.byteOrder);


        if(this.fitsTable.getCommpressed() && !this.hasReadCalibrationConstants){
            Utils.mapContainsKeys(item, "Data", "StartCellData", "NROI", "NPIX");
            short[] data = ((short[])item.get("Data"));
            short[] startCellData = (short[])item.get("StartCellData");
            int roi = (int) item.get("NROI");
            int numberOfPixel = (int) item.get("NPIX");

//            if (calibData==null)
//                throw new NullPointerException("Should not happen");

            applyDrsOffsetCalib(roi, numberOfPixel, data, startCellData, this.calibrationConstants);
            item.put("Data", data);
        }
//		log.debug(item.toString());
        return item;
    }

    private Data readDataFromBytes(Data item, byte[][] dataRow, ZFitsTable table, ByteOrder byteOrder) throws ParseException {
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
                        for (int i=0; i<bArray.length; i++)
                            bArray[i] = data[i];
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
        if(this.drsFile != null){
            item.put("@drsFile", this.drsFile);
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

    @Override
    public void setDrsFile(File drsFile) {
        this.drsFile = drsFile;
    }
}
