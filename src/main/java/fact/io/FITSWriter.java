package fact.io;

import nom.tam.fits.*;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.data.DataFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Created by maxnoe on 10.08.16.
 *
 * Write data to a FITS file sequentially. The advantage over FITSWriter is that not the whole
 * BinaryTable has to be in memory until the end of the stream.
 *
 * This processor is able to serialize scalars and 1d-arrays of fixed length containing primitive types.
 * Other data structures will be ignored or might lead to errors.
 *
 * The fits file is initialised using the given keys from first data item.
 * All following items must have the same structure.
 *
 */
public class FITSWriter implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(FITSWriter.class);

    @Parameter(required = true)
    private Keys keys = new Keys("");

    @Parameter(required = true)
    private URL url;

    @Parameter(defaultValue ="Events", description = "EXTNAME for the binary table extension")
    private String extname = "Events";

    private String[] defaultKeys = {"EventNum", "TriggerType", "NROI", "NPIX"};

    private BufferedFile bf;
    private ByteBuffer buffer;
    private BinaryTableHDU bhdu;
    private boolean initialized = false;
    private long rowSize;
    private long numEventsWritten = 0;

    static ArrayList<String> columnNames = new ArrayList<>(0);
    static HashMap<String, Integer> columnLength = new HashMap<>(0);


    @Override
    public Data process(Data item) {
        Data outputItem = DataFactory.create();

        for (String key: defaultKeys ){
            outputItem.put(key, item.get(key));
        }
        for (String key: keys.select(item) ){
            outputItem.put(key, item.get(key));
        }

        if (!initialized){
            try {
                log.info("Initialising output fits file");
                initFITSFile(outputItem);
                initialized = true;
            } catch (FitsException e){
                throw new RuntimeException("Could not initialize fits file", e);
            }
        }

        try {
            writeRow(outputItem);
            numEventsWritten +=1;
        } catch (IOException e) {
            throw new RuntimeException("Error writing data to FITS file", e);
        }

        return item;
    }

    private void writeRow(Data item) throws IOException {
        buffer.clear();
        for (String columnName: columnNames){
            Serializable elem = item.get(columnName);
            Class<? extends Serializable> type = elem.getClass();
            if (type.isArray()) {
                if (elem instanceof int[]) {
                    int[] arr = (int[]) elem;
                    for (int val: arr){
                        buffer.putInt(val);
                    }
                } else if (elem instanceof double[]) {
                    double[] arr = (double[]) elem;
                    for (double val: arr){
                        buffer.putDouble(val);
                    }
                } else if (elem instanceof byte[]) {
                    byte[] arr = (byte[]) elem;
                    buffer.put(arr);
                } else if (elem instanceof String[]) {
                    String[] arr = (String[]) elem;
                    for(String val: arr){
                        buffer.put(val.getBytes());
                    }
                } else if (elem instanceof float[]) {
                    float[] arr = (float[]) elem;
                    for (float val: arr){
                        buffer.putFloat(val);
                    }
                } else if (elem instanceof short[]) {
                    short[] arr = (short[]) elem;
                    for (short val: arr){
                        buffer.putShort(val);
                    }
                } else if (elem instanceof long[]) {
                    for (long val: (long[]) elem){
                        buffer.putLong(val);
                    }
                } else if (elem instanceof boolean[]) {
                    for(boolean val: (boolean[]) elem){
                        buffer.put((byte) (val ? 1 : 0));
                    }
                } else {
                    throw new RuntimeException("Serializable cannot be saved to FITS");
                }
            } else {
                if (ClassUtils.isAssignable(type, String.class)) {
                    buffer.put(((String) elem).getBytes());
                } else if (ClassUtils.isAssignable(type, Integer.class)) {
                    buffer.putInt((Integer) elem);
                } else if (ClassUtils.isAssignable(type, Double.class)) {
                    buffer.putDouble((Double) elem);
                } else if (ClassUtils.isAssignable(type, Float.class)) {
                    buffer.putFloat((Float) elem);
                } else if (ClassUtils.isAssignable(type, Short.class)) {
                    buffer.putShort((Short) elem);
                } else if (ClassUtils.isAssignable(type, Long.class)) {
                    buffer.putLong((Short) elem);
                } else if (ClassUtils.isAssignable(type, Boolean.class)) {
                    buffer.put((byte) ((Boolean) elem ? 1 : 0));
                } else {
                    throw new RuntimeException("Serializable cannot be saved to FITS");
                }
            }
        }

        buffer.flip();
        bf.write(buffer.array());
    }

    private void addSerializableToArrayList(Serializable serializable, ArrayList<Object> arrayList) throws RuntimeException {
        Class<? extends Serializable> type = serializable.getClass();

        // if the value is an array, we can directly add it
        if (type.isArray()) {
            arrayList.add(serializable);
        } else {
            // primitive values need to be wrapped into an array of length 1
            if (ClassUtils.isAssignable(type, String.class)) {
                arrayList.add(new String[]{(String) serializable});
            } else if (ClassUtils.isAssignable(type, Integer.class)) {
                arrayList.add(new int[]{(int) serializable});
            } else if (ClassUtils.isAssignable(type, Double.class)) {
                arrayList.add(new double[]{(double) serializable});
            } else if (ClassUtils.isAssignable(type, Float.class)) {
                arrayList.add(new float[]{(float) serializable});
            } else if (ClassUtils.isAssignable(type, Short.class)) {
                arrayList.add(new short[]{(short) serializable});
            } else if (ClassUtils.isAssignable(type, Long.class)) {
                arrayList.add(new long[]{(long) serializable});
            } else if (ClassUtils.isAssignable(type, Boolean.class)) {
                arrayList.add(new boolean[]{(boolean) serializable});
            } else {
                throw new RuntimeException("Serializable cannot be saved to FITS");
            }
        }
    }


    private void initFITSFile(Data item) throws  FitsException{
        ArrayList<Object> rowList = new ArrayList<>();
        for (String columnName: item.keySet()) {
            Serializable serializable = item.get(columnName);

            try {
                addSerializableToArrayList(serializable, rowList);
                columnNames.add(columnName);
            } catch (RuntimeException e) {
                log.warn("Key {} is not writable to FITS file, will be skipped", columnName);
            }
        }

        Object[] row = rowList.toArray();
        rowSize = ArrayFuncs.computeLSize(row);
        log.info("Row size is {} bytes", rowSize);
        BinaryTable table = new BinaryTable();
        table.addRow(row);
        Header header = new Header();
        table.fillHeader(header);
        bhdu = new BinaryTableHDU(header, table);
        log.info("created bhdu with {} columns", bhdu.getData().getNCols());
        buffer = ByteBuffer.allocate((int) rowSize);

        for (int i=0; i < columnNames.size(); i++){
            bhdu.setColumnName(i, columnNames.get(i), null);
        }
        bhdu.getHeader().addValue("EXTNAME", extname, "name of extension table");
        bhdu.getHeader().write(bf);
    }


    @Override
    public void init(ProcessContext processContext) throws Exception {
        FitsFactory.setUseAsciiTables(false);
        bf = new BufferedFile(url.getFile(), "rw");
        bf.setLength(0); // clear current file content

        // We first have to write an empty header because a binary table cannot be the first hdu
        BasicHDU.getDummyHDU().write(bf);
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        FitsUtil.pad(bf, rowSize * numEventsWritten);
        bf.close();
        Fits fits = new Fits(url.getPath());
        BinaryTableHDU bhdu = (BinaryTableHDU) fits.getHDU(1);
        bhdu.getHeader().setNaxis(2, (int) numEventsWritten);
        bhdu.getHeader().rewrite();

    }

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setExtname(String extname) {
        this.extname = extname;
    }
}
