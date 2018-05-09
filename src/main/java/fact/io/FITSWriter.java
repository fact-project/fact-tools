package fact.io;

import fact.VersionInformation;
import nom.tam.fits.*;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


/**
 * Created by maxnoe on 10.08.16.
 * <p>
 * Write data to a FITS file sequentially.
 * <p>
 * This processor is able to serialize scalars and 1d-arrays of fixed length containing primitive types.
 * Other data structures will be ignored (complex objects) or lead to errors (variable length arrays).
 * <p>
 * The fits file is initialised and the header is filled using the given keys from first data item.
 * All following items must have the same structure.
 */
public class FITSWriter extends Writer implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(FITSWriter.class);

    @Parameter(description = "Keys to save to the outputfile, if not given, the default keys for observations and simulations are stored, taken from the default/settings.properties file")
    public Keys keys = null;

    @Parameter(description = "Keys to write to the FITS Header. Only the first data item will be used. Using streams.Keys")
    public Keys headerKeys = new Keys("");

    @Parameter(required = true, description = "Url for the output file")
    public URL url;

    @Parameter(defaultValue = "Events", description = "EXTNAME for the binary table extension")
    public String extname = "Events";

    @Parameter(required = false, description = "Set if you want to allow non existing keys.")
    public boolean allowNullKeys = false;

    private BufferedFile bf;
    private ByteBuffer buffer;
    private BinaryTableHDU bhdu;
    private boolean initialized = false;
    private long rowSize;

    private Set<String> previousKeySet;
    long numEventsWritten = 0;

    ArrayList<String> columnNames = new ArrayList<>(0);
    HashMap<String, int[]> columnDimensions = new HashMap<>(0);


    @Override
    public Data process(Data item) {
        if (keys == null) {
            log.info("Getting default outputkeys");
            keys = getDefaultKeys(isSimulated(item));
        }

        Data outputItem = DataFactory.create();

        for (String key : keys.select(item)) {
            outputItem.put(key, item.get(key));
        }
        testKeys(outputItem, keys, allowNullKeys);

        if (!initialized) {
            try {
                log.info("Initialising output fits file");

                Data headerItem = DataFactory.create();
                for (String key : headerKeys.select(item)) {
                    log.debug("Saving key {} to header", key);
                    headerItem.put(key, item.get(key));
                }

                initFITSFile(outputItem, headerItem);
                initialized = true;
            } catch (FitsException e) {
                throw new RuntimeException("Could not initialize fits file", e);
            }
        }

        try {
            writeRow(outputItem);
            numEventsWritten += 1;
        } catch (IOException e) {
            throw new RuntimeException("Error writing data to FITS file", e);
        }

        return item;
    }

    private void writeRow(Data item) throws IOException {
        buffer.clear();
        for (String columnName : columnNames) {
            Serializable unwrappedElem = item.get(columnName);

            Object elem = wrapInArray(unwrappedElem);

            if (!Arrays.equals(columnDimensions.get(columnName), ArrayFuncs.getDimensions(elem))) {
                throw new RuntimeException("Dimensions of key " + columnName + " changed. FITSWriter does not support variable length arrays");
            }

            if (elem instanceof int[]) {
                int[] arr = (int[]) elem;
                for (int val : arr) {
                    buffer.putInt(val);
                }
            } else if (elem instanceof double[]) {
                double[] arr = (double[]) elem;
                for (double val : arr) {
                    buffer.putDouble(val);
                }
            } else if (elem instanceof byte[]) {
                byte[] arr = (byte[]) elem;
                buffer.put(arr);
            } else if (elem instanceof String[]) {
                String[] arr = (String[]) elem;
                for (String val : arr) {
                    buffer.put(val.getBytes());
                }
            } else if (elem instanceof float[]) {
                float[] arr = (float[]) elem;
                for (float val : arr) {
                    buffer.putFloat(val);
                }
            } else if (elem instanceof short[]) {
                short[] arr = (short[]) elem;
                for (short val : arr) {
                    buffer.putShort(val);
                }
            } else if (elem instanceof long[]) {
                for (long val : (long[]) elem) {
                    buffer.putLong(val);
                }
            } else if (elem instanceof boolean[]) {
                for (boolean val : (boolean[]) elem) {
                    buffer.put((byte) (val ? 1 : 0));
                }
            } else {
                throw new RuntimeException("Serializable cannot be saved to FITS");
            }

        }

        buffer.flip();
        bf.write(buffer.array());
    }

    private void fillHeader(Header header, Data item) {
        for (String key : item.keySet()) {
            Serializable serializable = item.get(key);

            if (key.length() > 8) {
                log.warn("Key {} too long, truncating to {}", key, key.substring(0, 8));
                key = key.substring(0, 8);
            }

            Class<? extends Serializable> type = serializable.getClass();
            try {
                if (ClassUtils.isAssignable(type, String.class)) {
                    header.addValue(key, (String) serializable, "");
                } else if (ClassUtils.isAssignable(type, Integer.class)) {
                    header.addValue(key, (int) serializable, "");
                } else if (ClassUtils.isAssignable(type, Double.class)) {
                    header.addValue(key, (double) serializable, "");
                } else if (ClassUtils.isAssignable(type, Float.class)) {
                    header.addValue(key, (float) serializable, "");
                } else if (ClassUtils.isAssignable(type, Short.class)) {
                    header.addValue(key, (short) serializable, "");
                } else if (ClassUtils.isAssignable(type, Long.class)) {
                    header.addValue(key, (long) serializable, "");
                } else if (ClassUtils.isAssignable(type, Boolean.class)) {
                    header.addValue(key, (boolean) serializable, "");
                } else if (ClassUtils.isAssignable(type, ZonedDateTime.class)) {
                    ZonedDateTime zonedDateTime = (ZonedDateTime) serializable;
                    String iso = formatDateTime(zonedDateTime);
                    header.addValue(key, iso, "");
                } else {
                    throw new RuntimeException("Key '" + key + "' cannot be saved to FITS Header");
                }
            } catch (HeaderCardException e) {
                log.error("Could not write key {} to FITS header", key);
                throw new RuntimeException(e);
            }
        }
    }

    private Object wrapInArray(Serializable serializable) throws RuntimeException {
        Class<? extends Serializable> type = serializable.getClass();

        // if the value is an array, we can directly add it
        if (type.isArray()) {
            return serializable;
        } else {
            // primitive values need to be wrapped into an array of length 1
            if (ClassUtils.isAssignable(type, String.class)) {
                return new String[]{(String) serializable};
            } else if (ClassUtils.isAssignable(type, Integer.class)) {
                return new int[]{(int) serializable};
            } else if (ClassUtils.isAssignable(type, Double.class)) {
                return new double[]{(double) serializable};
            } else if (ClassUtils.isAssignable(type, Float.class)) {
                return new float[]{(float) serializable};
            } else if (ClassUtils.isAssignable(type, Short.class)) {
                return new short[]{(short) serializable};
            } else if (ClassUtils.isAssignable(type, Long.class)) {
                return new long[]{(long) serializable};
            } else if (ClassUtils.isAssignable(type, Boolean.class)) {
                return new boolean[]{(boolean) serializable};
            } else if (ClassUtils.isAssignable(type, ZonedDateTime.class)) {
                ZonedDateTime zonedDateTime = (ZonedDateTime) serializable;
                String iso = formatDateTime(zonedDateTime);
                return new String[]{iso};
            } else {
                throw new RuntimeException("Serializable cannot be saved to FITS");
            }
        }
    }


    private void initFITSFile(Data item, Data headerItem) throws FitsException {
        ArrayList<Object> rowList = new ArrayList<>();
        for (String columnName : item.keySet()) {
            Serializable serializable = item.get(columnName);
            try {
                Object elem = wrapInArray(serializable);
                rowList.add(elem);
                columnDimensions.put(columnName, ArrayFuncs.getDimensions(elem));
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
        header.addValue(
                "VERSION",
                VersionInformation.getInstance().gitDescribe,
                "The FACT-Tools Version used to write this file"
        );
        table.fillHeader(header);
        fillHeader(header, headerItem);

        bhdu = new BinaryTableHDU(header, table);

        log.info("created bhdu with {} columns", bhdu.getData().getNCols());
        buffer = ByteBuffer.allocate((int) rowSize);

        for (int i = 0; i < columnNames.size(); i++) {
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
        BasicHDU bhdu = BasicHDU.getDummyHDU();
        bhdu.getHeader().addValue(
                "VERSION",
                VersionInformation.getInstance().gitDescribe,
                "The FACT-Tools Version used to write this file"
        );
        bhdu.write(bf);
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        if (initialized) {
            FitsUtil.pad(bf, rowSize * numEventsWritten);
            bf.close();
            Fits fits = new Fits(url.getPath());
            BinaryTableHDU bhdu = (BinaryTableHDU) fits.getHDU(1);
            bhdu.getHeader().setNaxis(2, (int) numEventsWritten);
            bhdu.getHeader().rewrite();
        } else {
            bf.close();
        }
    }

    /**
     * Java DateTimeFormatter.ISO_INSTANT creates variable length strings, because
     * only the significant digits are returned.
     * Thus, we pad the string and return only the microseconds part.
     *
     * @param zonedDateTime
     * @return
     */
    public static String formatDateTime(ZonedDateTime zonedDateTime) {
        String iso = zonedDateTime.format(DateTimeFormatter.ISO_INSTANT);
        iso = iso.replace("Z", "");
        if (iso.length() == 19) {
            iso = iso + ".";
        }
        iso = StringUtils.rightPad(iso, 26, "0");
        iso = iso.substring(0, 26);
        return iso + "Z";
    }
}
