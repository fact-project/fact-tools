/**
 *
 */
package fact.utils;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processors changes the order of the pixels in the data from SoftId to Chid
 *
 * @author kai
 */
public class Remapping implements Processor {
    static Logger log = LoggerFactory.getLogger(Remapping.class);

    @Parameter(required = true, description = "Key refering to an array of short containing pixel data sorted by SoftId")
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, short[].class);

        short[] data = (short[]) input.get(key);

        short[] remapped = new short[data.length];
        remapFromSoftIdToChid(data, remapped);

        input.put(outputKey, remapped);
        return input;
    }

    public void remapFromSoftIdToChid(short[] data, short[] remapped) {
        int roi = data.length / Constants.N_PIXELS;
        for (int softId = 0; softId < Constants.N_PIXELS; softId++) {
            int chid = FactPixelMapping.getInstance().getChidFromSoftID(softId);
            System.arraycopy(data, softId * roi, remapped, chid * roi, roi);
        }
    }
}
