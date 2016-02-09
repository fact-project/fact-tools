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
 *
 */
public class Remapping implements Processor{
    static Logger log = LoggerFactory.getLogger(Remapping.class);

    @Parameter(required = true, defaultValue = "raw:dataCalibrated",
            description = "Key refering to an array of short containing pixel data sorted by SoftId")
    private String inputKey     ="raw:dataCalibrated";
    @Parameter(required = true, defaultValue = "raw:dataCalibrated")
    private String outputKey    ="raw:dataCalibrated";

    private int npix = Constants.NUMBEROFPIXEL;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, inputKey, short[].class);
        Utils.isKeyValid(item, "NPIX", Integer.class);

        short[] data = (short[]) item.get(inputKey);
        npix = (Integer) item.get("NPIX");

        short[] remapped = new short[data.length];
        remapFromSoftIdToChid(data, remapped);

        item.put(outputKey, remapped);
        return item;
    }

    public void remapFromSoftIdToChid(short[] data, short[] remapped) {
        int roi = data.length/ npix;
        for(int softId = 0; softId < npix; softId++){
            int chid = FactPixelMapping.getInstance().getChidFromSoftID(softId);
            System.arraycopy(data, softId*roi, remapped, chid*roi, roi );
        }
    }
}
