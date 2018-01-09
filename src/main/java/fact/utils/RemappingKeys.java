package fact.utils;

import fact.Utils;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.io.Serializable;
import java.lang.reflect.Array;


/**
 * The monte carlo values are written in softId order. We have to remap those values to be ordered by chid.
 */
public class RemappingKeys implements Processor {
    static Logger log = LoggerFactory.getLogger(RemappingKeys.class);

    @Parameter(required = true)
    String keys[] = null;

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        if (keys == null) {
            log.error("No key specified");
            throw new RuntimeException("You have to specify the key to remap");
        }
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Serializable value = null;
            if (input.containsKey(key)) {
                value = input.get(key);
            } else {
                throw new RuntimeException("Could not get key: " + key + " from data item");
            }
            Object arrayInSoftID = null;
            Object arrayInCHID = null;
            if (value.getClass().isArray()) {
                //
                Class<?> type = value.getClass().getComponentType();
                int length = 0;
                if (type == float.class) {
                    float[] temp = ((float[]) value);
                    length = temp.length;
                    arrayInSoftID = Array.newInstance(type, length);
                    arrayInSoftID = temp;
                    arrayInCHID = Array.newInstance(type, length);
                } else if (type == double.class) {
                    double[] temp = ((double[]) value);
                    length = temp.length;
                    arrayInSoftID = Array.newInstance(type, length);
                    arrayInSoftID = temp;
                    arrayInCHID = Array.newInstance(type, length);
                } else if (type == int.class) {
                    int[] temp = ((int[]) value);
                    length = temp.length;
                    arrayInSoftID = Array.newInstance(type, length);
                    arrayInSoftID = temp;
                    arrayInCHID = Array.newInstance(type, length);
                } else if (type == String.class) {
                    String[] temp = ((String[]) value);
                    length = temp.length;
                    arrayInSoftID = Array.newInstance(type, length);
                    arrayInSoftID = temp;
                    arrayInCHID = Array.newInstance(type, length);
                } else {
                    throw new RuntimeException("The key: " + key + " is of type: " + type + ". Only float,double,int,String supported at the moment!");
                }
                if (length != npix) {
                    throw new RuntimeException("The length of key: " + key + " is not equal to the Number of Pixels: " + npix);
                }
            } else {
                throw new RuntimeException("The key: " + key + " is not an array");
            }
            for (int softId = 0; softId < npix; softId++) {
                int chid = FactPixelMapping.getInstance().getChidFromSoftID(softId);
                System.arraycopy(arrayInSoftID, softId, arrayInCHID, chid, 1);
            }
            // TODO Auto-generated method stub
            input.put(key, (Serializable) arrayInCHID);
        }
        return input;
    }
}
