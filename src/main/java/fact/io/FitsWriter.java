package fact.io;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.common.FitsException;
import nom.tam.util.BufferedFile;
import org.apache.commons.lang3.ClassUtils;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author alexey
 */
public class FitsWriter implements StatefulProcessor {

    static int counter = 0;

    @Override
    public void init (ProcessContext processContext) throws Exception {

    }

    @Override
    public void resetState () throws Exception {

    }

    @Override
    public void finish () throws Exception {

    }

    @Override
    public Data process (Data data) {
        Fits fits = new Fits();
        for (String key : data.keySet()) {
            Serializable serializable = data.get(key);
            try {
                addHDU(fits, key, serializable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedFile bf = new BufferedFile("test" + counter++ + ".fits", "rw");
            fits.write(bf);
            bf.close();
        } catch (FitsException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addHDU (Fits fits, String key, Serializable serialized) throws FitsException {
        Object[] objects = new Object[2];
        objects[0] = new String[]{key};
        Class<? extends Serializable> type = serialized.getClass();
        if (type.isArray()) {
            if (serialized instanceof int[]) {
                int[] arr = (int[]) serialized;
                fits.addHDU(FitsFactory.hduFactory(arr));
                return;
            } else if (serialized instanceof double[]) {
                double[] arr = (double[]) serialized;
                fits.addHDU(FitsFactory.hduFactory(arr));
                return;
            } else if (serialized instanceof byte[]) {
                byte[] arr = (byte[]) serialized;
                objects= new Object[]{key, arr};
                fits.addHDU(FitsFactory.hduFactory(objects));
                return;
            } else {
                return;
            }
        } else {
            if (ClassUtils.isAssignable(type, String.class)) {
                objects[1] = new String[]{(String) serialized};
            } else if (ClassUtils.isAssignable(type, Integer.class)) {
                objects[1] = new int[]{(int) serialized};
            } else if (ClassUtils.isAssignable(type, Double.class)) {
                objects[1] = new double[]{(double) serialized};
            } else if (ClassUtils.isAssignable(type, Float.class)) {
                objects[1] = new float[]{(float) serialized};
            } else if (ClassUtils.isAssignable(type, Short.class)) {
                objects[1] = new short[]{(short) serialized};
            } else if (ClassUtils.isAssignable(type, Long.class)) {
                objects[1] = new long[]{(long) serialized};
            } else if (ClassUtils.isAssignable(type, Boolean.class)) {
                objects[1] = new boolean[]{(boolean) serialized};
            } else {
                return;
            }
        }
        fits.addHDU(FitsFactory.hduFactory(objects));
    }

    private void addColumnnToTable (BinaryTable table, Serializable serialized) throws FitsException {
        Class<? extends Serializable> type = serialized.getClass();
        if (ClassUtils.isAssignable(type, String.class)) {
            String[] strings = {(String) serialized};
            table.addColumn(strings);
        } else if (ClassUtils.isAssignable(type, Integer.class)) {
            int[] integers = {(int) serialized};
            table.addColumn(integers);
        } else if (ClassUtils.isAssignable(type, Double.class)) {
            double[] doubles = {(double) serialized};
            table.addColumn(doubles);
        } else if (ClassUtils.isAssignable(type, Float.class)) {
            float[] floats = {(float) serialized};
            table.addColumn(floats);
        } else if (ClassUtils.isAssignable(type, Short.class)) {
            short[] shorts = {(short) serialized};
            table.addColumn(shorts);
        } else if (ClassUtils.isAssignable(type, Long.class)) {
            long[] longs = {(long) serialized};
            table.addColumn(longs);
        }
    }
}
