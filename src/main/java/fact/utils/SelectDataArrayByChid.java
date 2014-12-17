package fact.utils;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;

import static java.util.Arrays.*;

/**
 * Created by jbuss on 20.11.14.
 */
public class SelectDataArrayByChid implements Processor {
    static Logger log = LoggerFactory.getLogger(SelectDataArrayByChid.class);
    @Parameter(required = true, description = "Key to the array you want the information about")
    private String key = null;
    @Parameter(required = true, description = "The name of the data written to the stream")
    private String outputKey = null;
    @Parameter(description = "Pixel ID of a desired Pixel")
    private int chid = 0;

    private int npix;

    @Override
    public Data process(Data input) {

        Utils.mapContainsKeys(input, key);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        double[] data = (double[]) input.get(key);

        int roi = data.length/npix;


        //add processors threshold to the DataItem
        input.put(outputKey, Arrays.copyOfRange(data, chid * roi, (chid + 1) * roi));

        return input;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public int getChid() {
        return chid;
    }

    public void setChid(int chid) {
        this.chid = chid;
    }
}
