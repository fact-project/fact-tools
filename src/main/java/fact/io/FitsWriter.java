package fact.io;

import nom.tam.fits.*;
import nom.tam.fits.common.FitsException;
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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author alexey
 */
public class FitsWriter implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(FitsWriter.class);

    @Parameter(required = true)
    private Keys keys = new Keys("");

    private final static String[] defaultKeys = {"EventNum", "TriggerType", "NROI", "NPIX"};

    public static final String TTYPE = "TTYPE";
    public static final String TFORM = "TFORM";
    public static final String NAXIS_1 = "NAXIS1";
    static int counter = 0;
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
        bf = new BufferedFile("test" + counter + ".fits", "rw");
    }

    @Override
    public void resetState () throws Exception {

    }

    @Override
    public void finish () throws Exception {
    }

    @Override
    public Data process (Data data) {
        // process keys
        try {
            for (String key : defaultKeys) {
                collectObjects(key, data.get(key));
            }

            for (String key : keys.select(data)) {
                collectObjects(key, data.get(key));
            }
        } catch (Exception e) {
            log.error("Collecting objects for FitsWriter thrown an exception." +
                    "Data will not be written.\nError message:{}", e.getMessage());
        }

        try {
            table.addRow(values.toArray());
            try {
                if (counter == 2) {
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
                    }

                    // retrieve the true NAXIS1 value
                    Header header = new Header();
                    table.fillHeader(header);

                    // set true NAXIS1 value in the basic hdu used for writing
                    HeaderCard naxis1 = header.findCard(NAXIS_1);
                    if (naxis1 != null) {
                        basicHDU.addValue(
                                naxis1.getKey(),
                                naxis1.getValue(),
                                naxis1.getComment());
                    }
                    fits.addHDU(basicHDU);
                    fits.write(bf);
                    bf.close();
                }
                counter++;
            } catch (IOException | FitsException e) {
                e.printStackTrace();
            }
            names.clear();
            values.clear();
        } catch (FitsException e) {
            e.printStackTrace();
        }
        return null;
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
}
