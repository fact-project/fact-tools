package fact.io;

import nom.tam.fits.*;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

/**
 * Write data with given keys into fits file.
 *
 * @author alexey
 */
public class FITSWriter implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(FITSWriter.class);

    @Parameter(required = true)
    private Keys keys = new Keys("");

    @Parameter(required = true)
    private URL url;

    @Parameter(defaultValue ="Events", description = "EXTNAME for the binary table extension")
    private String extname = "Events";

    private final static String[] defaultKeys = {"EventNum", "TriggerType", "NROI", "NPIX"};

    public static final String TTYPE = "TTYPE";
    public static final String TFORM = "TFORM";
    public static final String NAXIS1 = "NAXIS1";
    private Fits fits;

    static ArrayList<String> names = new ArrayList<>(0);
    static ArrayList<Object> values = new ArrayList<>(0);
    private BinaryTable table;
    private BufferedFile bf;

    @Override
    public void init (ProcessContext processContext) throws Exception {
        fits = new Fits();
        table = new BinaryTable();
        //TODO output selection
        bf = new BufferedFile(url.getFile(), "rw");
    }

    @Override
    public void resetState () throws Exception {

    }

    @Override
    public void finish () throws Exception {
        //FIXME pcount is wrong
        BasicHDU<?> basicHDU = FitsFactory.hduFactory(table.getData());
        Cursor<String, HeaderCard> iterator = basicHDU.getHeader().iterator();
        while (iterator.hasNext()) {
            HeaderCard next = iterator.next();
            if (next.getKey().startsWith(TFORM)) {
                String key = next.getKey().toLowerCase();
                String substring = key.substring(5);
                int tformNumber = Integer.valueOf(substring);
                HeaderCard headerCard = new HeaderCard(
                        TTYPE + tformNumber, // 'TTYPE'+number
                        names.get(tformNumber - 1), // ttype
                        ""); // comment
                basicHDU.getHeader().addLine(headerCard);
            }

            // remove information about dimensionality as it is described by TFORM
            if (next.getKey().startsWith("TDIM")) {
                basicHDU.getHeader().deleteKey(next.getKey());
            }
        }

        // retrieve the true NAXIS1 value
        Header header = new Header();
        table.fillHeader(header);

        // set true NAXIS1 value in the basic hdu used for writing
        HeaderCard naxis1 = header.findCard(NAXIS1);
        if (naxis1 != null) {
            try {
                basicHDU.addValue(
                        naxis1.getKey(),
                        Integer.valueOf(naxis1.getValue()),
                        naxis1.getComment());
            } catch (NumberFormatException exc) {
                log.error("{} couldn't have been converted to integer value.",
                        naxis1.getValue());
            }
        }
        basicHDU.getHeader().addValue("EXTNAME", extname, "name of extension table");
        fits.addHDU(basicHDU);
        fits.write(bf);
        bf.close();
    }

    @Override
    public Data process (Data item) {
        // process keys
        try {
            for (String key : defaultKeys) {
                collectObjects(key, item.get(key));
            }

            for (String key : keys.select(item)) {
                collectObjects(key, item.get(key));
            }
        } catch (Exception e) {
            log.error("Collecting objects for FITSWriter thrown an exception." +
                    "Data will not be written.\nError message:{}", e.getMessage());
        }

        try {
            table.addRow(values.toArray());
            values.clear();
        } catch (FitsException e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * Consider given serialized object and save it to array list of values if
     * it has primitive JAVA type. Complex objects are escaped.
     *
     * @param key        object name
     * @param serialized serialized object from data item
     * @throws FitsException
     */
    private void collectObjects (String key, Serializable serialized)
            throws FitsException {
        Class<? extends Serializable> type = serialized.getClass();
        // save number of values to decide afterwards if the key should be
        // saved or not (some complex structures are ignored)
        int oldValuesCount = values.size();
        if (type.isArray()) {
            // add array of primitive values
            if (serialized instanceof int[]) {
                int[] arr = (int[]) serialized;
                values.add(arr);
            } else if (serialized instanceof double[]) {
                double[] arr = (double[]) serialized;
                values.add(arr);
            } else if (serialized instanceof byte[]) {
                byte[] arr = (byte[]) serialized;
                values.add(arr);
            } else if (serialized instanceof String[]) {
                String[] arr = (String[]) serialized;
                values.add(arr);
            } else if (serialized instanceof float[]) {
                float[] arr = (float[]) serialized;
                values.add(arr);
            } else if (serialized instanceof short[]) {
                short[] arr = (short[]) serialized;
                values.add(arr);
            } else if (serialized instanceof long[]) {
                long[] arr = (long[]) serialized;
                values.add(arr);
            } else if (serialized instanceof boolean[]) {
                boolean[] arr = (boolean[]) serialized;
                values.add(arr);
            }
        } else {
            // add single primitive value as array of length 1
            if (ClassUtils.isAssignable(type, String.class)) {
                values.add(new String[]{(String) serialized});
            } else if (ClassUtils.isAssignable(type, Integer.class)) {
                values.add(new int[]{(int) serialized});
            } else if (ClassUtils.isAssignable(type, Double.class)) {
                values.add(new double[]{(double) serialized});
            } else if (ClassUtils.isAssignable(type, Float.class)) {
                values.add(new float[]{(float) serialized});
            } else if (ClassUtils.isAssignable(type, Short.class)) {
                values.add(new short[]{(short) serialized});
            } else if (ClassUtils.isAssignable(type, Long.class)) {
                values.add(new long[]{(long) serialized});
            } else if (ClassUtils.isAssignable(type, Boolean.class)) {
                values.add(new boolean[]{(boolean) serialized});
            }
        }

        // add key to the list of object names if given object has been
        // added to the list of values
        if (oldValuesCount < values.size()) {
            if (!names.contains(key)) {
                names.add(key);
            }
        }
    }

    public void setUrl(URL url) {

        this.url = url;
    }

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    public void setExtname(String extname) {
        this.extname = extname;
    }
}

