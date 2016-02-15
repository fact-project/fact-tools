package fact.io;

import nom.tam.fits.*;
import nom.tam.fits.common.FitsException;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;
import org.apache.commons.lang3.ClassUtils;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author alexey
 */
public class FitsWriter implements StatefulProcessor {

    public static final String TTYPE = "TTYPE";
    public static final String TFORM = "TFORM";
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
        //TODO key selection
        for (String key : data.keySet()) {
            Serializable serializable = data.get(key);
            try {
                collectObjects(key, serializable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Object[] valuesArr = values.toArray();
            table.addRow(valuesArr);
            try {
                if (counter == 2) {
                    //FIXME pcount is wrong
                    BasicHDU<?> basicHDU = FitsFactory.hduFactory(table.getData());
                    Cursor<String, HeaderCard> iterator = basicHDU.getHeader().iterator();
                    while (iterator.hasNext()) {
                        HeaderCard next = iterator.next();
                        if (next.getKey().startsWith(TFORM)) {
                            String s = next.getKey().toLowerCase();
                            String substring = s.substring(5);
                            int tformNumber = Integer.valueOf(substring);
                            String ttype = names.get(tformNumber - 1);
                            HeaderCard headerCard = new HeaderCard(
                                    TTYPE + tformNumber, ttype, "");
                            basicHDU.getHeader().addLine(headerCard);
                        }
                    }

                    // retrieve the true NAXIS1 value
                    Header header = new Header();
                    table.fillHeader(header);

                    // set true NAXIS1 value in the basic hdu used for writing
                    HeaderCard naxis1 = header.findCard("NAXIS1");
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

    private void collectObjects (String key, Serializable serialized) throws FitsException {
        Class<? extends Serializable> type = serialized.getClass();
        if (type.isArray()) {
            if (serialized instanceof int[]) {
                int[] arr = (int[]) serialized;
                names.add(key);
                values.add(arr);
            } else if (serialized instanceof double[]) {
                double[] arr = (double[]) serialized;
                values.add(arr);
                names.add(key);
            } else if (serialized instanceof byte[]) {
                byte[] arr = (byte[]) serialized;
                values.add(arr);
                names.add(key);
            } else if (serialized instanceof String[]) {

            } else if (serialized instanceof float[]) {

            } else if (serialized instanceof short[]) {

            } else if (serialized instanceof long[]) {

            } else if (serialized instanceof boolean[]) {

            }
        } else {
            if (ClassUtils.isAssignable(type, String.class)) {
                values.add(new String[]{(String) serialized});
                names.add(key);
            } else if (ClassUtils.isAssignable(type, Integer.class)) {
                values.add(new int[]{(int) serialized});
                names.add(key);
            } else if (ClassUtils.isAssignable(type, Double.class)) {
                values.add(new double[]{(double) serialized});
                names.add(key);
            } else if (ClassUtils.isAssignable(type, Float.class)) {
                values.add(new float[]{(float) serialized});
                names.add(key);
            } else if (ClassUtils.isAssignable(type, Short.class)) {
                values.add(new short[]{(short) serialized});
                names.add(key);
            } else if (ClassUtils.isAssignable(type, Long.class)) {
                values.add(new long[]{(long) serialized});
                names.add(key);
            } else if (ClassUtils.isAssignable(type, Boolean.class)) {
                values.add(new boolean[]{(boolean) serialized});
                names.add(key);
            }
        }
    }
}
