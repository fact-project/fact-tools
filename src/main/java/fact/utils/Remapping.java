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
 * @author kai, Michael Bulinski &lt;michael.bulinski@udo.edu&gt;
 */
public class Remapping implements Processor {
    static Logger log = LoggerFactory.getLogger(Remapping.class);

    @Parameter(required = true, description = "Key refering to an array of short containing pixel data sorted by SoftId")
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Parameter(required = false, description = "Whether to remap back to softid from chid")
    public boolean reverse;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, short[].class);

        short[] data = (short[]) item.get(key);

        short[] remapped = new short[data.length];
        if (!reverse)
            remapFromSoftIdToChid(data, remapped);
        else
            remapFromChidToSoftId(data, remapped);

        item.put(outputKey, remapped);
        return item;
    }

    public void remapFromSoftIdToChid(short[] data, short[] remapped) {
        int roi = data.length / Constants.N_PIXELS;
        for (int softId = 0; softId < Constants.N_PIXELS; softId++) {
            int chid = FactPixelMapping.getInstance().getChidFromSoftID(softId);
            System.arraycopy(data, softId * roi, remapped, chid * roi, roi);
        }
    }

    public void remapFromChidToSoftId(short[] data, short[] remapped) {
        int roi = data.length / Constants.N_PIXELS;
        for (int softId = 0; softId < Constants.N_PIXELS; softId++) {
            int chid = FactPixelMapping.getInstance().getSoftIDFromChid(softId);
            System.arraycopy(data, softId * roi, remapped, chid * roi, roi);
        }
    }
}
